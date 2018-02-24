package cn.bestwu.lang.packagescan;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.apache.tomcat.util.buf.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageScanClassResolver {

  private Logger log = LoggerFactory.getLogger(PackageScanClassResolver.class);
  private Set<ClassLoader> classLoaders;
  private Set<PackageScanFilter> scanFilters;
  private Map<String, Set<Class>> allClassesByPackage = new HashMap<>();
  private Set<String> loadedPackages = new HashSet<>();

  private Map<String, Set<String>> classFilesByLocation = new HashMap<>();

  public void addClassLoader(ClassLoader classLoader) {
    try {
      getClassLoaders().add(classLoader);
    } catch (UnsupportedOperationException ex) {
      // Ignore this exception as the PackageScanClassResolver
      // don't want use any other classloader
    }
  }

  public void addFilter(PackageScanFilter filter) {
    if (scanFilters == null) {
      scanFilters = new LinkedHashSet<>();
    }
    scanFilters.add(filter);
  }

  public void removeFilter(PackageScanFilter filter) {
    if (scanFilters != null) {
      scanFilters.remove(filter);
    }
  }

  public Set<ClassLoader> getClassLoaders() {
    if (classLoaders == null) {
      classLoaders = new HashSet<>();
      ClassLoader ccl = Thread.currentThread().getContextClassLoader();
      if (ccl != null) {
        log.debug("The thread context class loader: " + ccl + "  is used to load the class");
        classLoaders.add(ccl);
      }
      classLoaders.add(PackageScanClassResolver.class.getClassLoader());
    }
    return classLoaders;
  }

  public void setClassLoaders(Set<ClassLoader> classLoaders) {
    this.classLoaders = classLoaders;
  }

  public Set<Class<?>> findImplementations(Class parent, String... packageNames) {
    if (packageNames == null) {
      return Collections.emptySet();
    }

    log.debug("Searching for implementations of " + parent.getName() + " in packages: " + Arrays
        .asList(packageNames));

    PackageScanFilter test = getCompositeFilter(new AssignableToPackageScanFilter(parent));
    Set<Class<?>> classes = new LinkedHashSet<>();
    for (String pkg : packageNames) {
      find(test, pkg, classes);
    }

    log.debug("Found: " + classes);

    return classes;
  }

  public Set<Class<?>> findByFilter(PackageScanFilter filter, String... packageNames) {
    if (packageNames == null) {
      return Collections.emptySet();
    }

    Set<Class<?>> classes = new LinkedHashSet<>();
    for (String pkg : packageNames) {
      find(filter, pkg, classes);
    }

    log.debug("Found: " + classes);

    return classes;
  }

  protected void find(PackageScanFilter test, String packageName, Set<Class<?>> classes) {
    packageName = packageName.replace('.', '/');

    Set<ClassLoader> set = getClassLoaders();

    if (!loadedPackages.contains(packageName)) {
      for (ClassLoader classLoader : set) {
        this.findAllClasses(packageName, classLoader);
      }
      loadedPackages.add(packageName);
    }

    findInAllClasses(test, packageName, classes);
  }

  protected void findAllClasses(String packageName, ClassLoader loader) {
    log.debug(
        "Searching for all classes in package: " + packageName + " using classloader: " + loader
            .getClass().getName());

    Enumeration<URL> urls;
    try {
      urls = getResources(loader, packageName);
      if (!urls.hasMoreElements()) {
        log.debug("No URLs returned by classloader");
      }
    } catch (IOException ioe) {
      log.warn("Cannot read package: " + packageName, ioe);
      return;
    }

    while (urls.hasMoreElements()) {
      URL url = null;
      try {
        url = urls.nextElement();
        log.debug("URL from classloader: " + url);

        url = customResourceLocator(url);

        String urlPath = url.getFile();
        String host = null;
        urlPath = URLDecoder.decode(urlPath, "UTF-8");

        if (url.getProtocol().equals("vfs") && !urlPath.startsWith("vfs")) {
          urlPath = "vfs:" + urlPath;
        }
        if (url.getProtocol().equals("vfszip") && !urlPath.startsWith("vfszip")) {
          urlPath = "vfszip:" + urlPath;
        }

        log.debug("Decoded urlPath: " + urlPath + " with protocol: " + url.getProtocol());

        // If it's a file in a directory, trim the stupid file: spec
        if (urlPath.startsWith("file:")) {
          // file path can be temporary folder which uses characters that the URLDecoder decodes wrong
          // for example + being decoded to something else (+ can be used in temp folders on Mac OS)
          // to remedy this then create new path without using the URLDecoder
          try {
            URI uri = new URI(url.getFile());
            host = uri.getHost();
            urlPath = uri.getPath();
          } catch (URISyntaxException e) {
            // fallback to use as it was given from the URLDecoder
            // this allows us to work on Windows if users have spaces in paths
          }

          if (urlPath.startsWith("file:")) {
            urlPath = urlPath.substring(5);
          }
        }

        // osgi bundles should be skipped
        if (url.toString().startsWith("bundle:") || urlPath.startsWith("bundle:")) {
          log.debug("It's a virtual osgi bundle, skipping");
          continue;
        }

        // Else it's in a JAR, grab the path to the jar
        if (urlPath.contains(".jar/") && !urlPath.contains(".jar!/")) {
          urlPath = urlPath.replace(".jar/", ".jar!/");
        }

        if (urlPath.indexOf('!') > 0) {
          urlPath = urlPath.substring(0, urlPath.indexOf('!'));
        }

        // If a host component was given prepend it to the decoded path.
        // This still has its problems as we silently skip user and password
        // information etc. but it fixes UNC urls on windows.
        if (host != null) {
          if (urlPath.startsWith("/")) {
            urlPath = "//" + host + urlPath;
          } else {
            urlPath = "//" + host + "/" + urlPath;
          }
        }

        File file = new File(urlPath);
        if (file.isDirectory()) {
          log.debug("Loading from directory using file: " + file);
          loadImplementationsInDirectory(packageName, file, loader);
        } else {
          InputStream stream;
          if (urlPath.startsWith("http:") || urlPath.startsWith("https:") || urlPath
              .startsWith("sonicfs:") || urlPath.startsWith("vfs:") || urlPath
              .startsWith("vfszip:")) {
            // load resources using http/https
            // sonic ESB requires to be loaded using a regular URLConnection
            URL urlStream = new URL(urlPath);
            log.debug("Loading from jar using " + urlStream.getProtocol() + ": " + urlPath);
            URLConnection con = urlStream.openConnection();
            // disable cache mainly to avoid jar file locking on Windows
            con.setUseCaches(false);
            stream = con.getInputStream();
          } else {
            log.debug("Loading from jar using file: " + file);
            stream = new FileInputStream(file);
          }

          try {
            loadImplementationsInJar(packageName, stream, loader, urlPath, null);
          } catch (IOException ioe) {
            log.warn(
                "Cannot search jar file '" + urlPath + "' for classes due to an IOException: " + ioe
                    .getMessage(), ioe);
          } finally {
            stream.close();
          }
        }
      } catch (IOException e) {
        // use debug logging to avoid being to noisy in logs
        log.debug("Cannot read entries in url: " + url, e);
      }
    }
  }

  protected void findInAllClasses(PackageScanFilter test, String packageName,
      Set<Class<?>> classes) {
    log.debug("Searching for: " + test + " in package: " + packageName);

    Set<Class> packageClasses = getFoundClasses(packageName);
    if (packageClasses == null) {
      log.debug("No classes found in package: " + packageName);
      return;
    }
    for (Class type : packageClasses) {
      if (test.matches(type)) {
        classes.add(type);
      }
    }

  }

  protected void addFoundClass(Class<?> type) {
    if (type.getPackage() != null) {
      String packageName = type.getPackage().getName();
      List<String> packageNameParts = Arrays.asList(packageName.split("\\."));
      for (int i = 0; i < packageNameParts.size(); i++) {
        String thisPackage = StringUtils.join(packageNameParts.subList(0, i + 1), '/');
        addFoundClass(thisPackage, type);
      }
    }
  }

  protected void addFoundClass(String packageName, Class<?> type) {
    packageName = packageName.replace("/", ".");

    if (!this.allClassesByPackage.containsKey(packageName)) {
      this.allClassesByPackage.put(packageName, new HashSet<Class>());
    }

    this.allClassesByPackage.get(packageName).add(type);
  }

  protected Set<Class> getFoundClasses(String packageName) {
    packageName = packageName.replace("/", ".");
    return this.allClassesByPackage.get(packageName);
  }

  // We can override this method to support the custom ResourceLocator

  protected URL customResourceLocator(URL url) {
    // Do nothing here
    return url;
  }

  /**
   * Strategy to get the resources by the given classloader.
   * to take care of WebSphere's odditiy of resource loading.
   *
   * @param loader the classloader
   * @param packageName the packagename for the package to load
   * @return URL's for the given package
   * @throws IOException is thrown by the classloader
   */
  protected Enumeration<URL> getResources(ClassLoader loader, String packageName)
      throws IOException {
    log.debug("Getting resource URL for package: " + packageName + " with classloader: " + loader);

    // If the URL is a jar, the URLClassloader.getResources() seems to require a trailing slash.  The
    // trailing slash is harmless for other URLs
    if (!packageName.endsWith("/")) {
      packageName = packageName + "/";
    }
    return loader.getResources(packageName);
  }

  private PackageScanFilter getCompositeFilter(PackageScanFilter filter) {
    if (scanFilters != null) {
      CompositePackageScanFilter composite = new CompositePackageScanFilter(scanFilters);
      composite.addFilter(filter);
      return composite;
    }
    return filter;
  }

  /**
   * Finds matches in a physical directory on a filesystem. Examines all files
   * within a directory - if the File object is not a directory, and ends with
   * <i>.class</i> the file is loaded. Operates recursively to find classes within a
   * folder structure matching the package structure.
   *
   * @param parent the package name up to this directory in the package
   * hierarchy. E.g. if /classes is in the classpath and we wish to
   * examine files in /classes/org/apache then the values of
   * <i>parent</i> would be <i>org/apache</i>
   * @param location a File object representing a directory
   */
  private void loadImplementationsInDirectory(String parent, File location,
      ClassLoader classLoader) {
    Set<String> classFiles = classFilesByLocation.get(location.toString());
    if (classFiles == null) {
      classFiles = new HashSet<>();

      File[] files = location.listFiles();
      StringBuilder builder;

      for (File file : files) {
        builder = new StringBuilder(100);
        String name = file.getName();
        if (name != null) {
          name = name.trim();
          builder.append(parent).append("/").append(name);
          String packageOrClass = parent == null ? name : builder.toString();

          if (file.isDirectory()) {
            loadImplementationsInDirectory(packageOrClass, file, classLoader);
          } else if (name.endsWith(".class")) {
            classFiles.add(packageOrClass);
          }
        }
      }
    }

    for (String packageOrClass : classFiles) {
      this.loadClass(packageOrClass, classLoader);
    }
  }

  private void loadClass(String className, ClassLoader classLoader) {
    try {
      String externalName = className.substring(0, className.indexOf('.')).replace('/', '.');
      Class<?> type = classLoader.loadClass(externalName);
      log.debug("Loaded the class: " + type + " in classloader: " + classLoader);

      addFoundClass(type);

    } catch (ClassNotFoundException e) {
      log.debug(
          "Cannot find class '" + className + "' in classloader: " + classLoader + ". Reason: " + e,
          e);
    } catch (LinkageError e) {
      log.debug(
          "Cannot find the class definition '" + className + "' in classloader: " + classLoader
              + ". Reason: " + e, e);
    } catch (Throwable e) {
      log.error(
          "Cannot load class '" + className + "' in classloader: " + classLoader + ".  Reason: "
              + e, e);
    }

  }

  /**
   * Finds matching classes within a jar files that contains a folder
   * structure matching the package structure. If the File is not a JarFile or
   * does not exist a warn will be logged, but no error will be raised.
   * <p>
   * Any nested JAR files found inside this JAR will be assumed to also be
   * on the classpath and will be recursively examined for classes in `parentPackage`.
   *
   * @param parentPackage the parent package under which classes must be in order to
   * be considered
   * @param parentFileStream the inputstream of the jar file to be examined for classes
   * @param loader a classloader which can load classes contained within the JAR file
   * @param parentFileName a unique name for the parentFileStream, to be used for caching.
   * This is the URL of the parentFileStream, if it comes from a URL,
   * or a composite ID if we are currently examining a nested JAR.
   * @param grandparentFileName grandparentFileName
   * @throws IOException IOException
   */
  protected void loadImplementationsInJar(String parentPackage, InputStream parentFileStream,
      ClassLoader loader, String parentFileName, String grandparentFileName) throws IOException {
    Set<String> classFiles = classFilesByLocation.get(parentFileName);

    if (classFiles == null) {
      classFiles = new HashSet<>();
      classFilesByLocation.put(parentFileName, classFiles);

      Set<String> grandparentClassFiles = classFilesByLocation.get(grandparentFileName);
      if (grandparentClassFiles == null) {
        grandparentClassFiles = new HashSet<>();
        classFilesByLocation.put(grandparentFileName, grandparentClassFiles);
      }
      JarInputStream jarStream;
      if (parentFileStream instanceof JarInputStream) {
        jarStream = (JarInputStream) parentFileStream;
      } else {
        jarStream = new JarInputStream(parentFileStream);
      }

      JarEntry entry;
      while ((entry = jarStream.getNextJarEntry()) != null) {
        String name = entry.getName();
        if (name != null) {
          if (name.endsWith(".jar")) { //in a nested jar
            log.debug("Found nested jar " + name);

            // To avoid needing to unzip 'parentFile' in its entirety, as that
            // may take a very long time (see CORE-2115) or not even be possible
            // (see CORE-2595), we load the nested JAR from the classloader and
            // read it as a zip.
            //
            // It is safe to assume that the nested JAR is readable by the classloader
            // as a resource stream, because we have reached this point by scanning
            // through packages located from `classloader` by using `getResource`.
            // If loading this nested JAR as a resource fails, then certainly loading
            // classes from inside it with `classloader` would fail and we are safe
            // to exclude it form the PackageScan.
            InputStream nestedJarResourceStream = loader.getResourceAsStream(name);
            if (nestedJarResourceStream != null) {
              try (JarInputStream nestedJarStream = new JarInputStream(nestedJarResourceStream)) {
                loadImplementationsInJar(parentPackage, nestedJarStream, loader,
                    parentFileName + "!" + name, parentFileName);
              }
            }
          } else if (!entry.isDirectory() && name.endsWith(".class")) {
            classFiles.add(name.trim());
            grandparentClassFiles.add(name.trim());
          }
        }
      }
    }

    for (String name : classFiles) {
      if (name.contains(parentPackage)) {
        loadClass(name, loader);
      }
    }
  }

}
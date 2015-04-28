#Project Structure and Process

This document explains the elements of the project structure

##Project Structure
The project will use the Maven build system and create two types artifacts. AEM packages that contain JCR repository content and OSGi bundles.  The structure of the project can be seen below.

![project-hierarchy](http://www.axis41.net/archetype-assets/project-hierarchy.png)

As depicted above, the Bundle Install package embeds the OSGi bundles into an install folder within the ``/apps/[appname]/install`` folder.

##Build Profiles
There are three Maven profiles that modify the build behavior. The `autoInstallPackage`, `autoInstallBundle`, and `includeTestContent` profiles are discussed below. Each profile can be enabled using the -P command line switch when building.

###autoInstallPackage
This profile applies only to the content-package type projects. The profile adds the `install` goal of the Maven Vault plugin to the `package` phase of the build lifecycle. The `install` goal uploads the built package to the CRX package manager of the AEM instance. The default configuration uploads to `http://localhost:4502` using `admin`/`admin` as the username/password. The location and credentials can be overridden by setting the properties seen in the root project POM. The -D switch can be used to set the properties on the command line. For example, 

	mvn -Dcrx.userId=foo -Dcrx.password=bar -DserviceUrl=http://otherdomain.com:4502

###autoInstallBundle
This profile applies to the bundle projects. The profile adds the `install` goal of the Maven Bundle plugin to the `package` phase of the build lifecycle. The install goal uploads the built bundle the the Felix OSGi container of a AEM instance. The default configuration uploads to localhost, port 4502 using admin/admin as the username/password. The location and credentials can be overridden by setting the properties seen in the root project POM. The -D switch can be used to set the properties on the command line.

###includeTestContent
By default the Test Content project is not included within the `modules` section of its parent reactor project(`packages`). Activating this profile adds the Test Content  project as a module in the build.

##Build Processes
The following section discusses several common build situations: the initial build, working on a component, updating content, working on a bundle, and adding a new bundle.

###Initial Build
The initial build will require installing the packages, bundles, and test content. Run 

    mvn clean install -PautoInstallPackage,includeTestContent 
	
from the project root. Note, by activating the `autoInstallPackage` profile, the Bundle Install package will be included.

###Application Module
The application module contains the `/apps` and `/etc` folders.

###Working on a Component
The only module necessary to build when working on a component is the application module. Navigate to the application module (`project-root/packages/application`) and run

	mvn clean install -PautoInstallPackage
	
By only building the application project no bundles will be built and copied which will significantly decrease the build time.

###Updating Content
Navigate to the test content module (`project-root/packages/test-content`) and run

	mvn clean install -PautoInstallPackage
	
The `includeTestContent` profile is only necessary if you want to include Test Content while building the parent reactor. 
	
###Working on a Bundle
Again, the specific bundle module being worked on is the only module necessary to build. Navigate to the bundle (`project-root/bundles/example`) and run

	mvn clean install -PautoInstallBundle
	
The configurations for the bundles are located in `/apps/config`. In order to update the configurations, go to `project-root/packages/bundle-install` and run

	mvn clean install -PautoInstallPackage
	
Running this command will also install the bundle to `/apps/[appname]/install` through the embeds. If you are working with the Bundle Install package, it is not necessary to build with `autoInstallBundle` the profile.

###Adding a New Bundle

Copy an existing bundle and edit the files to add a new bundle. In the `pom.xml` of the new bundle (called `newbundle`, for example), be sure to change the `artifactId`

    <artifactId>newbundle</artifactId>
    
and the `Export-Package` element to specify the publically available Java packages.

    <Export-Package>
    	com.newbundle.newservice.*
    </Export-Package>
    
The `pom.xml` belonging to the Bundles project(the new bundle's parent project) must be edited to include the new child bundle

    <module>newbundle</module>
    
Finally, the Bundle Install pom needs to be updated. Add a dependency

    <dependency>
    	<groupId>groupId.bundles</groupId>
        <artifactId>newbundle</artifactId>
        <version>${project.version}</version>
    </dependency>
    
and an embed
	
	<embedded>
            <groupId>groupId.bundles</groupId>
            <artifactId>newbundle</artifactId>
            <filter>true</filter>
	</embedded>
	
Build the new bundle by running 

	mvn clean install -PautoInstallBundle
	
in the new module.

If configurations are necessary for the new bundle, create a new `.xml` file in `project-root/packages/bundle-install/src/main/content/jcr_root/apps/project-name/config`. The name of the file needs to match the PID of the service you are trying to configure. To build the configurations, go to `project-root/packages/bundle-install` and run

	mvn clean install -PautoInstallPackage
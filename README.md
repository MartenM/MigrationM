# MigrationM
A simple migration manager for Java applications.

### ⭐ Features
* Makes updating a database easy.
* Pure .sql files (or any other extension)
* Don't bother users with executing queries manually
* Simple way to store migrations in the resource folder
* (Possible) support for different databases

### 📃 Description
Exporting programs to users is fun, but when a database needs changes it often becomes quite a pain. This is especially true for SQL languages.

MigrationM offers a simple and easy to use migration system that makes changing the database easy.
By applying migration files the database can be build from the ground up or updated when required.

Migrations can be stored as simple files in the resource folder. MigrationM provides an easy way to load these files.

Using a custom DataBaseManager it's also easy to support other languages than SQL.

## ⚙ Usage
An example project has been provided in the MigrationM-Example folder.
A basic rundown can be read here.

#### 1. Create the migrations
Create a folder in your resource folder. For this example we will use the folder name `migrations`.

After creating the folder create your migrations and put them into the folder. The name of the migrations HAS to be the following format: `yyyy-m-d-hh:mm-description.extension`. The `-description` part is not required but recommended.

#### 2. Add the maven dependencies to your project
Base module required for basic functionalities:
```xml
<dependency>
    <groupId>nl.martenm</groupId>
    <artifactId>MigrationM-Base</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>compile</scope>
</dependency>
```

Different databases might require different DataBaseManagers. That's why these are seperate submodules.
For MySql the following module has been tested.
```xml
<dependency>
    <groupId>nl.martenm</groupId>
    <artifactId>MigrationM-SQL</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>compile</scope>
</dependency>
```

#### 3. Add the MigrationM manager to the startup of your program
Add the MigrationM manager to the startup of your program.
In order to load the migrations in the `migrations` folder simply call the method `.loadMigrations(folder, RootClass.class)`.
After that you can simply call `.migrate()` and the migrations will execute automatically.
```jave
migrationManager = new MigrationM(
                new ProgramInfo("Name", "1.0.0", logger),
                new SQLDatabaseManager(dataSource));

migrationManager.loadMigrations("migrations", ExamplePlugin.class);
boolean success = migrationManager.migrate();
```

#### 4. Have a party 🎉
Have a party, you don't have to worry about writing confusing code in order to update your database.
While you are partying, don't forget to ⭐ this repository.

## 📎 Maven repository
Currently there is no central public repository for this project.
You can clone the project your self and install the MigrationM-Base and MigrationM-SQL by running the `mvn clean install` command.

##  ❓ FAQ
#### I already have an existing project and I want to include this!
That's great! When writing queries for the first migration do keep in mind that there might already exist a table (e.g. users of previous versions).

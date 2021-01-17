#MigrationM
A simple migration manager for Java applications.

###Features
* Makes updating a database easy.
* Pure .sql files (or any other extension)
* Simple way to store migrations in the resource folder
* (Possible) support for different databases

###Description
Exporting programs to users is fun, but when a database needs changes it often becomes quite a pain. This is especially true for SQL languages.

MigrationM offers a simple and easy to use migration system that makes chancing the database easy.
By applying migration files the database can be build from the ground up or updated when required.

Migrations can be stored as simple files in the resource folder. MigrationM provides an easy way to load these files.

Using a custom DataBaseManager it's also easy to support other languages than SQL.

##Usage
**1. Create the migrations**

Create a folder in your resource folder. For this example we will use the folder name `migrations`.

After creating the folder create your migrations and put them into the folder. The name of the migrations HAS to be the following format: `yyyy-m-d-hh:mm-descrption.extension`. The -description is not required but recommended.

**2. 

##Maven
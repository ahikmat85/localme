Automatic source code string values translation(localization)
=======

LocalMe

Utility to extract String values from Source code, and generate excel file for feeding translation. 
Then puts back the translated string  into source code.

Current condition, it retrieves the texts(strings) valuse from project which consists UTF-8(Korean,Chinese,Japanese)..

This project was done to accelerate localization of existing java project. 
It helps to avoid manual translation of whole source code.

What it does:

1) Goes through each file and subfolders recursively and find files which are java,xml, mxml..
2) In each file, read every line, and searchs for quote marks, and get the text inside quote marks, then puts in hashmap to avoid dublicates.
3) Creates Excel file, puts all the strings in Excel sheet, including in which file(s) and what line the string was.
4) After you put all translations to the left side of each string, it puts the string back into source files, replacing the old with new translation.

It works in any project with any kind of files. 

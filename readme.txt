If you are using some sort of Unix and have the "make" application installed, then you should be
able to enter "make run" to build and then run the application in a single command. To run tests,
enter "make test".

If I had the opportunity to do this project again, I think the first thing I would change is to use
a special java build system such as Gradle rather than using a very complicated Makefile.
I would also like to use some kind of management system to deal with the secret keys and values
rather than to just write them into strings and blank them out when needed, but I felt this went a
little outside the scope of the assessment.

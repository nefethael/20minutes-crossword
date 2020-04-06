20minutes-crossword
===================

20min retreives daily 20minutes crosswords and prints it with solution.

- Set default printer to valid one
- Edit 20min.bat to select how many pages you need
- Launch 20min.sh script 
	./20min [nb_pages_to_print] [true|false add solutions]
- Enjoy

Details
=======

1. download PDF to 20minutes website
2. parse PDF with seek20min/PDFBox to seek for crossword page and crossword number
3. print PDF page 
4. download solution from courbis website
5. print solution file

Dependencies
============

* PDFBox (2.0.16)
https://github.com/apache/pdfbox
Apache License 2.0

* JSoup (1.11-3)
https://github.com/jhy/jsoup
MIT

* Commons IO (2.6)
https://github.com/apache/commons-io
Apache License 2.0

* Commons Logging (1.2)
https://github.com/apache/commons-logging
Apache License 2.0

* 20minutes 
https://www.20minutes.fr/

* Courbis
http://www.courbis.fr

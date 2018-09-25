20minutes-crossword
===================

20min retreives daily 20minutes crosswords and prints it with solution.

- Set default printer to valid one
- Open unix style terminal on Windows
- Launch 20min.sh script 
	./20min [nb_pages_to_print]
- Enjoy

Details
=======

1. download PDF to 20minutes website
2. parse PDF with seek20min/PDFBox to seek for crossword page and crossword number
3. print PDF page with SumatraPDF
4. download solution from courbis website
5. print solution file

Dependencies
============

* Cygwin or eq. with curl
http://www.cygwin.com/
LGPLv3

* SumatraPDF (3.1.2.0)
https://github.com/sumatrapdfreader/sumatrapdf
GPLv3

* PDFBox (2.0.10)
https://github.com/apache/pdfbox
Apache License 2.0

* 20minutes 
https://www.20minutes.fr/

* Courbis
http://www.courbis.fr

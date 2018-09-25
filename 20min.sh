datebin=/usr/bin/date

DATE=`$datebin +%Y%m%d`
YEAR=`$datebin +%Y`
NB=$1

if [ "_$NB" = "_" ]
then
	NB=1
fi

rm -rf out.txt out2.txt screen1.png screen2.png screen3.png solution.txt

if [ ! -f ${DATE}_PAR.pdf ]
then 
	curl http://pdf.20mn.fr/$YEAR/quotidien/${DATE}_PAR.pdf -O
fi

echo "search page"
java -jar seek20min.jar "${DATE}_PAR.pdf" 2>/dev/null | while read PAGE NUM; 
do 
	NUM=`echo $NUM | awk '{printf("%d\n", $0)}'`
	./SumatraPDF.exe -print-settings "$PAGE,$NBx" -print-to-default  "${DATE}_PAR.pdf"
	curl "http://www.courbis.fr/Solution-de-la-grille-No-${NUM}.html" |\
	grep frwiktionary | sed 's#<[^>]*>##g' > solution.txt
	./SumatraPDF.exe -print-to-default solution.txt
done

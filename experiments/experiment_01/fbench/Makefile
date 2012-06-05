
#   Options for compiling and linking C programs

COPTS = -O3 -lm

WEBDIR = $(HOME)/web/fbench

#   Options for compiling and linking FORTRAN programs
FORTRANOPTS = -O3

#   Options for compiling and linking Ada programs
ADAOPTS = -O3

PROGRAMS = fbench ffbench

all:	$(PROGRAMS)

fbench: fbench.c
	cc fbench.c -o fbench $(COPTS)

ffbench: ffbench.c
	cc ffbench.c -o ffbench $(COPTS)

fortran:	fortran/fbench.f Makefile
	( cd fortran ; gfortran $(FORTRANOPTS) fbench.f -o fbench )
	
ada:	ada/fbench.adb Makefile
	( cd ada ; rm -f fbench.o fbench ; gnatmake $(ADAOPTS) fbench.adb )
	
pascal: pascal/fbench_borland.p Makefile
	( cd pascal; make clean; make )

dist:
	rm -f fbench.zip
	zip -r fbench.zip Makefile fbench.c ffbench.c \
	    ada/fbench.adb \
	    fortran/fbench.f \
	    java javascript lisp/fbench.lsp mbasic \
	    pascal \
	    perl python ruby scilab \
	    smalltalk/fbench.st smalltalk/results.txt \
	    visualbasic

release:
	cp -p fbench.zip $(WEBDIR)

clean:
	rm -f $(PROGRAMS) fortran/fbench *.o *.out core *.bak *.zip
	( cd pascal; make clean )


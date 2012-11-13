tmp=tmp

all: jar

jar:
	mkdir -p $(tmp)
	cp -R src/* $(tmp)/
	cp -R bin/* $(tmp)/
	jar cf ga-all.jar -C $(tmp) .
	rm -r $(tmp)/net/tailriver/science/ga/demo
	rm -r $(tmp)/net/tailriver/science/ga/test
	jar cf ga.jar -C $(tmp) .
	rm -r $(tmp)

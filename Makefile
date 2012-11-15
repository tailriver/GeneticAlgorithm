tmp=tmp

all: jar

jar:
	mkdir -p $(tmp)
	cp -R src/* $(tmp)/
	cp -R bin/* $(tmp)/
	rm -r $(tmp)/net/tailriver/science/ga/test
	jar cf ga-demo.jar -C $(tmp) .
	rm -r $(tmp)/net/tailriver/science/ga/demo
	jar cf ga.jar -C $(tmp) .
	rm -r $(tmp)

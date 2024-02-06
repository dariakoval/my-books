start:
	APP_ENV=development gradle run

clean:
	gradle clean

build:
	gradle clean build

test:
	gradle test

report:
	gradle jacocoTestReport

lint:
	gradle checkstyleMain checkstyleTest

.PHONY: build
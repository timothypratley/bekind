.PHONY: all setup serve update test lint

all: setup test

setup:
	clojure -P

serve:
	clojure -M:serve

update:
	clojure -M:outdated --every --write

test:
	clojure -M:dev:test

lint:
	clojure -M:lint

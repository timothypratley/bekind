.PHONY: all setup serve host update test lint

all: setup test

setup:
	clojure -P

test:
	clojure -M:dev:test

serve:
	clojure -M:serve

host:
	ngrok http 3000

update:
	clojure -M:outdated --every --write

lint:
	clojure -M:lint

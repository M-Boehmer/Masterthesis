#!/bin/bash

for i in worker1 worker2 worker3 worker4 worker5 worker6 worker7 workeri1 workeri2
do
 docker build -t l3oehmer/$i -f Docker/Dockerfile . --target $i
 docker push l3oehmer/$i
done
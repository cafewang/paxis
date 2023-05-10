docker build -t paxis:v1 .
kind create cluster --config=cluster.yaml
kind load docker-image paxis:v1 --name cluster
kubectl delete -f deploy.yaml
kubectl apply -f deploy.yaml

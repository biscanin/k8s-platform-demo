#!/usr/bin/env bash
# Registers the prod cluster in Argo CD (running on k3d-dev) by creating a cluster Secret.
# The token is read from prod and written straight into the dev cluster; it is never
# printed and never stored in Git.
set -euo pipefail

PROD_SERVER="${PROD_SERVER:-https://192.168.178.53:6443}"

TOKEN=$(kubectl --context prod -n kube-system get secret argocd-manager-token -o jsonpath='{.data.token}' | base64 -d)
CA=$(kubectl --context prod -n kube-system get secret argocd-manager-token -o jsonpath='{.data.ca\.crt}')

kubectl --context k3d-dev -n argocd create secret generic cluster-prod \
  --from-literal=name=prod \
  --from-literal=server="$PROD_SERVER" \
  --from-literal=config="{\"bearerToken\":\"$TOKEN\",\"tlsClientConfig\":{\"caData\":\"$CA\"}}"
kubectl --context k3d-dev -n argocd label secret cluster-prod argocd.argoproj.io/secret-type=cluster
echo "prod registered in Argo CD"

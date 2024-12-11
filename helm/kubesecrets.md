echo "Provide E-Invoice secret details"
read -s -p "DMS Token Secret: " dms_token_secret
echo ""
read -s -p "Microsoft Token Secret: " ms_token_secret
echo ""
read -s -p "Camunda Token Secret: " camunda_token_secret
echo ""
read -s -p "Billtrust Token Secret: " billtrust_token_secret
echo ""
read -s -p "Billtrust Password: " billtrust_password
echo ""
kubectl create secret generic einvoice-token-secrets --from-literal=azure.einvoice.dmsTokenSecret=$dms_token_secret --from-literal=azure.einvoice.msTokenSecret=$ms_token_secret  --from-literal=azure.einvoice.camundaTokenSecret=$camunda_token_secret --from-literal=azure.einvoice.billtrustTokenSecret=$billtrust_token_secret --from-literal=azure.einvoice.billtrustPassword=$billtrust_password -n einvoice
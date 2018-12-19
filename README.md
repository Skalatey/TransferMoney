# Трансфер денег между счетами
По умолчанию созданы два счета:
 getAll - http://localhost:8080/accounts
 get - http://localhost:8080/accounts/{id}

 transfer - http://localhost:8080/transaction/transfer
  {"idToAccount":1,"idFromAccount":2,"amount":2000}
  
 deposit - http://localhost:8080/transaction/deposit
  {"id":1,"amount":1000.00}
  
 withdrawal - http://localhost:8080/transaction/withdrawal
  {"id":2,"amount":222.222}

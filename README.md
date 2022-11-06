# PixStringGenerator
Simples gerador de String PIX para ser usado como "copia e cola" ou para criar QR codes.

Como quase todos os campos do PIX são sempre iguais pra basicamente a totalidade das transações realizadas,
deixei esses campos como Strings fixas.
O gerador reconhece o tipo de chave pix utilizada automaticamente, identificando se é CPF, CNPJ, E-mail, Telefone ou Aleatória.

Caso seja um telefone, informar SEM o +55! Utilize somente os 11 números do telefone com o DDD.
EX: 85912345678

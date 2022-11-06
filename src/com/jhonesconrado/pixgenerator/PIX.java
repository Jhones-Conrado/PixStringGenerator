/*
 * The MIT License
 *
 * Copyright 2022 jhonesconrado.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.jhonesconrado.pixgenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Simples gerador de String PIX para ser usado como "copia e cola" ou para gerar
 * QR codes.
 * @author jhonesconrado
 * +55 85 9 82050424
 */
public final class PIX {
    
    private final String pixKey;
    private final String description;
    private final String merchantName;
    private final String merchantCity;
    private final String merchantCep;
    private final String txid;
    private final BigDecimal amount;
    
    /**
     * Inicia uma nova instância de Pix para gerar Strings.
     * @param pixKey Chave PIX, podendo ser qualquer tipo.
     * @param description Descrição do pagamento.
     * @param merchantName Nome de quem está recebendo.
     * @param merchantCity Cidade de quem está recebendo.
     * @param merchantCep CEP de quem está recebendo.
     * @param txid ID do pagamento, por exemplo: Pedido 10.
     * @param amount Valor do pagamento.
     */
    public PIX(String pixKey, String description, String merchantName, String merchantCity, String merchantCep, String txid, BigDecimal amount) throws Exception {
//        Filtro da chave de pix.
//      Chave aleatória.
        if(pixKey.trim().length() == 36 && pixKey.trim().split("-").length == 5){
            this.pixKey = pixKey;
        } else if(pixKey.contains("@") && pixKey.contains(".")){ //Chave de e-mail.
            this.pixKey = pixKey;
        } else if(CpfValidator(pixKey)){//Verifica se é um CPF.
            this.pixKey = pixKey;
        } else if(pixKey.trim().replaceAll("[^0-9]", "").length() == 14){//Verifica se é um cnpj.
            this.pixKey = pixKey.trim().replaceAll("[^0-9]", "");
        } else if(pixKey.trim().replaceAll("[^0-9]", "").length() == 11){//Verifica se é um telefone.
            this.pixKey = "+55" + pixKey.trim().replaceAll("[^0-9]", "");
        } else {
            throw new Exception("Chave pix inválida.");
        }
        
        if(description != null){
            this.description = description;
        } else {
            this.description = "";
        }
        
        if(merchantName != null){
            this.merchantName = merchantName;
        } else {
            this.merchantName = "desconhecido";
        }
        
        if(merchantCity != null){
            this.merchantCity = merchantCity;
        } else {
            this.merchantCity = "desconhecido";
        }
        
        if(merchantCep != null){
            this.merchantCep = merchantCep;
        } else {
            this.merchantCep = "desconhecido";
        }
        
        if(txid != null){
            this.txid = txid;
        } else {
            this.txid = "desconhecido";
        }
        
        if(amount != null){
            this.amount = amount;
        } else {
            this.amount = new BigDecimal(0.00);
        }
    }

    public String getCode(){
//        ALGUMAS INFORMÇAÕES IMPORTANTES.
        /*
        00 = link do banco central.
        01 = chave pix.
        02 = descrição do pix.
        52 = categoria do mercador (a pessoa que está vendendo, recebendo o pagamento).
        53 = Código da moeda de transação.
        54 = valor do pagamento.
        58 = sigla do país.
        59 = nome do recebedor.
        60 = cidade do recebedor.
        61 = CEP do recebedor.
        62 = Identificador do pix. Campos adicionais.
        05 = o próprio indentificador, que vem logo após o campo 62.
        63 = tamanho do código.
        */
        
        StringBuilder code = new StringBuilder();
        //Entrada
        code.append("00020126");
        code.append(toSize("0014BR.GOV.BCB.PIX01"+toSize(this.pixKey, 2)+this.pixKey, 2));
        code.append("0014BR.GOV.BCB.PIX01");
        code.append(toSize(this.pixKey, 2));
        code.append(this.pixKey);
        
        //Cabeçalho e tamanho da descrição.
        code.append("02");
        code.append(toSize(this.description, 2));
        
        //Descrição.
        code.append(this.description);
        
        //Pos descrição
        code.append("520400005303986");
        
        //Valor do pagamento.
        code.append("54");
        code.append(toSize(this.amount.setScale(2, RoundingMode.HALF_EVEN).toString(), 2));
        code.append(this.amount.setScale(2, RoundingMode.HALF_EVEN).toString());
        
        //Sigla do país.
        code.append("5802BR");
        
        //Nome do recebedor.
        code.append("59");
        code.append(toSize(this.merchantName, 2));
        code.append(this.merchantName);
        
        //Cidade do recebedor.
        code.append("60");
        code.append(toSize(this.merchantCity, 2));
        code.append(this.merchantCity);
        
        //Cep do recebedor.
        code.append("61");
        code.append(toSize(this.merchantCep.replace("[^0-9]", ""), 2));
        code.append(this.merchantCep.replace("[^0-9]", ""));
        
        //Campos adicionais.
        code.append("62");
        code.append(toSize(getTxIdWithSize(), 2));
        code.append(getTxIdWithSize());
        
        //Validação crc16
        code.append("6304");
        
        int crc16 = crc16(code.toString().getBytes(), 0, code.toString().length());
        code.append(dechex(crc16).toUpperCase());
        return code.toString();
    }
    
    private String getTxIdWithSize(){
        return "05"+toSize(this.txid, 2)+this.txid;
    }
    
    private int crc16(byte[] data, int offset, int length) {
        if (data == null || offset < 0 || offset > data.length - 1 || offset + length > data.length) {
            return 0;
        }

        int crc = 0xFFFF;
        for (int i = 0; i < length; ++i) {
            crc ^= data[offset + i] << 8;
            for (int j = 0; j < 8; ++j) {
                crc = (crc & 0x8000) > 0 ? (crc << 1) ^ 0x1021 : crc << 1;
            }
        }
        return crc & 0xFFFF;
    }
    
    /**
     * Recebe uma string e adiciona 0 (zeros) a esquerda até completar o tamanho
     * informado como parâmetro.</br>
     * Por exemplo: Entrei com o valor "6", será retornado "06" caso o tamanho
     * informado seja 2.
     * @param str String que ficará no final.
     * @param size Tamanho total que deverá ter a string resultante.
     * @return String preenchida com zeros a esquerda.
     */
    private String toSize(String str, int size){
        String t = "";
        int strSize = str.length();
        String s = String.valueOf(strSize);
        if(s.length() < size){
            for(int i = 0 ; i < size-s.length() ; i++){
                t = t+"0";
            }
        }
        return t+String.valueOf(str.length());
    }
    
    /**
     * Recebe um inteiro e adiciona 0 (zeros) a esquerda até completar o tamanho
     * informado como parâmetro.</br>
     * Por exemplo: Entrei com o valor "6", será retornado "06" caso o tamanho
     * informado seja 2.
     * @param valor int que ficará no final.
     * @param size Tamanho total que deverá ter a string resultante.
     * @return String preenchida com zeros a esquerda.
     */
    private String toSize(int valor, int size){
        String  str = String.valueOf(valor);
        return toSize(str, size);
    }
    
    /**
     * Recebe um número inteiro e retorna a String em hexadecimal.
     * @param number
     * @return 
     */
    private String dechex(int number){
        if(number < 0){
            number = 0xffffffff + number + 1;
        }
        int parse = Integer.parseInt(String.valueOf(number), 10);
        return Integer.toHexString(parse);
    }
    
    /**
     * Recebe um array de char e verifica se é um CPF válido.
     * @param crrs Array de chars do CPF.
     * @return Verdadeiro caso seja um CPF válido.
     */
    public boolean CpfValidator(char[] crrs){
        if(crrs.length == 11){
            int a = 0, b = 0;
            int temp = 0;
            for(int i = 1 ; i <= 9 ; i++){
                temp += (Integer.parseInt(new String(new char[]{crrs[i-1]})))*i;
            }
            if(temp%11 != 10){
                a = temp%11;
            }
            temp = 0;
            for(int i = 0 ; i < 10 ; i++){
                temp += (Integer.parseInt(new String(new char[]{crrs[i]})))*i;
            }
            if(temp%11 != 10){
                b = temp%11;
            }

            String c = new String(crrs);
            return c.endsWith(String.valueOf(a)+String.valueOf(b));
        }
        return false;
    }
    
    /**
     * Recebe um CPF em forma de String e verifica se é válido.
     * @param str
     * @return 
     */
    public boolean CpfValidator(String str){
        String clear = str.trim().replaceAll("[^0-9]", "");
        if(clear.length() == 11){
            return CpfValidator(clear.toCharArray());
        }
        return false;
    }
    
}

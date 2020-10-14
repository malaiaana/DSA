package com.company;

import java.math.BigInteger;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        BigInteger H, max, min, q, p, g;
        long l, n, tmpHash = 32;

        // Генерация параметров
        min = BigInteger.valueOf(2).pow(32); // минимальная граница 2^32
        max = new BigInteger("1");
        for (int i = 32; i >= 0; i--) {
            max = max.add(BigInteger.valueOf(2).pow(i)); //считает верхнюю границу для рандома ~2^35
        }
        H = hash("hell", tmpHash);
        l = tmpHash * 8; // По формуле из википедии 2048/256 = 8
        do {
            q = createPrime(min, max);
            n = q.bitLength();
        } while (n != l);
        do {
            p = createPrime(min, max);
        } while (new BigInteger("2").pow((int) l - 1).compareTo(p) < 0 &&
                p.compareTo(new BigInteger("2").pow((int) l)) < 0 &&
                p.subtract(BigInteger.ONE).mod(q).compareTo(BigInteger.ZERO) != 0);
        do {
            g = BigInteger.valueOf(2).modPow((p.subtract(BigInteger.ONE)).divide(q), p);
        } while (g.compareTo(BigInteger.ONE) == 0);

        //Создание ключей х, у
        BigInteger x, y;
        do {
            BigInteger diff = max.subtract(min);
            Random randNum = new Random();
            int len = max.bitLength();
            x = new BigInteger(len, randNum);  //генерация секретног ключа
        } while (x.compareTo(BigInteger.ONE) == 1 && x.compareTo(q) == -1);
        y = g.modPow(x, p);  //открытый ключ для проверки подписи

        //Подпись сообщения k, r, s
        BigInteger k, r, s;
        do {
            BigInteger diff = q.subtract(min);
            Random randNum = new Random();
            int len = max.bitLength();
            k = new BigInteger(len, randNum);
            r = g.modPow(k, p).mod(q);
            s = k.modInverse(q).multiply(H.add(x).multiply(r)).mod(q);
        } while (k.compareTo(BigInteger.ONE) == 1 && r.compareTo(BigInteger.ZERO) == 0 && s.compareTo(BigInteger.ZERO) == 0);

        System.out.println("Подпись: " + r + ", " + s);

        //Проверка подписи w, u1, u2, v || v = r
        BigInteger w, u1, u2, v;
        w = s.modInverse(q);
        u1 = H.multiply(w).mod(q);
        u2 = r.multiply(w).mod(q);
        v = (g.modPow(u1, p).multiply(y.modPow(u2, p))).mod(p).mod(q); // цыганские фокусы со свойством mod
        if (v.compareTo(r) == 0) {
            System.out.println("Подпись верна!");
        }
    }

    public static BigInteger createPrime(BigInteger min, BigInteger max) {
        BigInteger bigInteger;
        BigInteger diff = max.subtract(min);
        Random randNum = new Random();
        int len = max.bitLength();
        do {
            bigInteger = new BigInteger(len, randNum);

        } while (!bigInteger.isProbablePrime(10));
        return bigInteger;
    }

    public static BigInteger hash(String m, long tmpHash) {
        byte[] messageBytes = m.getBytes();
        BigInteger messageAsDecimal = new BigInteger(messageBytes); //перевод массива байт в число
        return messageAsDecimal.mod(BigInteger.valueOf(tmpHash));
    }
}
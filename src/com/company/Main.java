package com.company;

import java.math.BigInteger;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        BigInteger H, max, min, q, p, g;
        long l, n, tmpHash = 32;

        // Генерация параметров
        int pow = 32;
        min = BigInteger.valueOf(2).pow(pow - 1); // минимальная граница 2^32
        max = new BigInteger("0");

        for (int i = pow - 1; i >= 0; i--) {
            max = max.add(BigInteger.valueOf(2).pow(i)); //считает верхнюю границу для рандома ~2^35
        }
        H = hash("hell", tmpHash);
        l = tmpHash * 8; // По формуле из википедии 2048/256 = 8

        do {
            q = createPrime(min, max);
            n = q.bitLength();
        }
        while (n != tmpHash);

        BigInteger minP = BigInteger.valueOf(2).pow((int) l - 1).add(BigInteger.ONE);
        BigInteger howManyTimesQisInP = minP.divide(q).add(BigInteger.ONE);
        do {
            p = q.multiply(howManyTimesQisInP).add(BigInteger.ONE);

            if (p.compareTo(new BigInteger("2").pow((int) l)) >= 0) {
                System.out.println("Выход за границу допустимых значений для p: 2^(l)");
                return;
            }

            howManyTimesQisInP = howManyTimesQisInP.add(BigInteger.ONE);
        }
        while (!p.isProbablePrime(10));

        BigInteger h = BigInteger.valueOf(2);
        do {
            g = h.modPow((p.subtract(BigInteger.ONE)).divide(q), p);
            h = h.add(BigInteger.ONE);
            if (h.compareTo(p.subtract(BigInteger.ONE)) >= 0) {
                System.out.println("Выход за границу допустимых значений для h: p-1");
            }
        }
        while (g.compareTo(BigInteger.ONE) == 0);

        //Создание ключей х, у
        BigInteger x, y;

//        BigInteger diff = q.subtract(BigInteger.ONE).subtract(BigInteger.ONE);
        Random randNum = new Random();
        int len = q.bitLength();
        do {
            x = new BigInteger(len, randNum);  //генерация секретног ключа
        }
        while (x.compareTo(BigInteger.ZERO) == 0 || x.compareTo(q) == 0);

        y = g.modPow(x, p);  //открытый ключ для проверки подписи

        //Подпись сообщения k, r, s
        BigInteger k, r, s;
        while (true) {
            randNum = new Random();
            len = q.bitLength();

            k = new BigInteger(len, randNum);
            if (k.compareTo(BigInteger.ZERO) == 0 || k.compareTo(q) == 0) {
                continue;
            }

            r = g.modPow(k, p).mod(q);
            if (r.compareTo(BigInteger.ZERO) == 0) {
                continue;
            }

            s = k.modInverse(q).multiply(H.add(x.multiply(r))).mod(q);
            if (s.compareTo(BigInteger.ZERO) == 0) {
                continue;
            }

            break;
        }
        System.out.println("Параметры: \nq = " + q + "\np = " + p + "\ng = " + g);
        System.out.println("x = " + x + "\ny = " + y);
        System.out.println("Подпись: \nr = " + r + "\ns = " + s);

        //Проверка подписи w, u1, u2, v || v = r
        BigInteger w, u1, u2, v;
        w = s.modInverse(q);
        u1 = H.multiply(w).mod(q);
        u2 = r.multiply(w).mod(q);
        v = (g.modPow(u1, p).multiply(y.modPow(u2, p))).mod(p).mod(q); // цыганские фокусы со свойством mod
        System.out.println("Проверка подписи:");
        System.out.println("v = " + v + "\nr = " + r);
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
            if (bigInteger.compareTo(min) < 0) {
                bigInteger = bigInteger.add(min);
            }
            if (bigInteger.compareTo(diff) >= 0) {
                bigInteger = bigInteger.mod(diff).add(min);
            }
        }
        while (!bigInteger.isProbablePrime(10));
        return bigInteger;
    }

    public static BigInteger hash(String m, long tmpHash) {
        byte[] messageBytes = m.getBytes();
        BigInteger messageAsDecimal = new BigInteger(messageBytes); //перевод массива байт в число
        return messageAsDecimal.mod(BigInteger.valueOf(tmpHash));
    }
}

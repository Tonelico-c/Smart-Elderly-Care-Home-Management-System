package com.situ.elder;

public class RecursionDemo {
    public static void main(String[] args) {
        System.out.println(factorial(5));
        System.out.println(factorial1(6));
    }

    public static int factorial(int n){
        if(n == 1){
            return 1;
        }
        return n*factorial(n - 1);
    }

    public static int factorial1(int n){
        int result = 1;
        for (int i = n; i >= 1; i--) {
            result *= i;
        }
        return result;
    }
}

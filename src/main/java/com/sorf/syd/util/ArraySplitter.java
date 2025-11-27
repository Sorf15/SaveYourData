package com.sorf.syd.util;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ArraySplitter<T> {

    //TODO: docs here
    @SuppressWarnings("unchecked")
    public static <V> ArrayList<V[]> splitArray(V[] array, V value, boolean flag) {
        int index = 0;
        int prev = 0;
        ArrayList<V[]> rawList = new ArrayList<>();
        for (; index < array.length; index++) {
            boolean valid = false;
            if (array[index] == value) {
                valid = true;
            } else if (index == array.length - 1 && flag) {
                valid = true;
                index++;
            }
            if (valid) {
                V[] a = (V[]) Array.newInstance(array.getClass(), index - prev);
                System.arraycopy(array, prev, a, 0, index - prev);
                rawList.add(a);
                prev = index + 1;
            }
        }
        return rawList;
    }

    public static ArrayList<int[]> splitArray(int[] array, int value, boolean flag) {
        int index = 0;
        int prev = 0;
        ArrayList<int[]> rawList = new ArrayList<>();
        for (; index < array.length; index++) {
            boolean valid = false;
            if (array[index] == value) {
                valid = true;
            } else if (index == array.length - 1 && flag) {
                valid = true;
                index++;
            }
            if (valid) {
                int[] a = new int[index - prev];
                System.arraycopy(array, prev, a, 0, index - prev);
                rawList.add(a);
                prev = index + 1;
            }
        }
        return rawList;
    }

    public static ArrayList<byte[]> splitArray(byte[] array, byte value, boolean flag) {
        int index = 0;
        int prev = 0;
        ArrayList<byte[]> rawList = new ArrayList<>();
        for (; index < array.length; index++) {
            boolean valid = false;
            if (array[index] == value) {
                valid = true;
            } else if (index == array.length - 1 && flag) {
                valid = true;
                index++;
            }
            if (valid) {
                byte[] a = new byte[index - prev];
                System.arraycopy(array, prev, a, 0, index - prev);
                rawList.add(a);
                prev = index + 1;
            }
        }
        return rawList;
    }

    public static ArrayList<long[]> splitArray(long[] array, long value, boolean flag) {
        int index = 0;
        int prev = 0;
        ArrayList<long[]> rawList = new ArrayList<>();
        for (; index < array.length; index++) {
            boolean valid = false;
            if (array[index] == value) {
                valid = true;
            } else if (index == array.length - 1 && flag) {
                valid = true;
                index++;
            }
            if (valid) {
                long[] a = new long[index - prev];
                System.arraycopy(array, prev, a, 0, index - prev);
                rawList.add(a);
                prev = index + 1;
            }
        }
        return rawList;
    }

    public static ArrayList<float[]> splitArray(float[] array, float value, boolean flag) {
        int index = 0;
        int prev = 0;
        ArrayList<float[]> rawList = new ArrayList<>();
        for (; index < array.length; index++) {
            boolean valid = false;
            if (array[index] == value) {
                valid = true;
            } else if (index == array.length - 1 && flag) {
                valid = true;
                index++;
            }
            if (valid) {
                float[] a = new float[index - prev];
                System.arraycopy(array, prev, a, 0, index - prev);
                rawList.add(a);
                prev = index + 1;
            }
        }
        return rawList;
    }

    public static ArrayList<double[]> splitArray(double[] array, double value, boolean flag) {
        int index = 0;
        int prev = 0;
        ArrayList<double[]> rawList = new ArrayList<>();
        for (; index < array.length; index++) {
            boolean valid = false;
            if (array[index] == value) {
                valid = true;
            } else if (index == array.length - 1 && flag) {
                valid = true;
                index++;
            }
            if (valid) {
                double[] a = new double[index - prev];
                System.arraycopy(array, prev, a, 0, index - prev);
                rawList.add(a);
                prev = index + 1;
            }
        }
        return rawList;
    }

    public static ArrayList<short[]> splitArray(short[] array, short value, boolean flag) {
        int index = 0;
        int prev = 0;
        ArrayList<short[]> rawList = new ArrayList<>();
        for (; index < array.length; index++) {
            boolean valid = false;
            if (array[index] == value) {
                valid = true;
            } else if (index == array.length - 1 && flag) {
                valid = true;
                index++;
            }
            if (valid) {
                short[] a = new short[index - prev];
                System.arraycopy(array, prev, a, 0, index - prev);
                rawList.add(a);
                prev = index + 1;
            }
        }
        return rawList;
    }

    public static ArrayList<char[]> splitArray(char[] array, char value, boolean flag) {
        int index = 0;
        int prev = 0;
        ArrayList<char[]> rawList = new ArrayList<>();
        for (; index < array.length; index++) {
            boolean valid = false;
            if (array[index] == value) {
                valid = true;
            } else if (index == array.length - 1 && flag) {
                valid = true;
                index++;
            }
            if (valid) {
                char[] a = new char[index - prev];
                System.arraycopy(array, prev, a, 0, index - prev);
                rawList.add(a);
                prev = index + 1;
            }
        }
        return rawList;
    }

    public static ArrayList<boolean[]> splitArray(boolean[] array, boolean value, boolean flag) {
        int index = 0;
        int prev = 0;
        ArrayList<boolean[]> rawList = new ArrayList<>();
        for (; index < array.length; index++) {
            boolean valid = false;
            if (array[index] == value) {
                valid = true;
            } else if (index == array.length - 1 && flag) {
                valid = true;
                index++;
            }
            if (valid) {
                boolean[] a = new boolean[index - prev];
                System.arraycopy(array, prev, a, 0, index - prev);
                rawList.add(a);
                prev = index + 1;
            }
        }
        return rawList;
    }
}

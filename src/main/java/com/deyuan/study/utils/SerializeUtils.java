package com.deyuan.study.utils;

import java.io.*;

public class SerializeUtils {

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream baos = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(baos);
        return ois.readObject();

    }


    public static byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        return baos.toByteArray();
    }
}

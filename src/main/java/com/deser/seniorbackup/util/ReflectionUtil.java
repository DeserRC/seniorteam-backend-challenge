package com.deser.seniorbackup.util;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil {
    private static final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.") [3];
    private static final String pathNMS = "net.minecraft.server." + version + ".";
    private static final String pathOBC = "org.bukkit.craftbukkit." + version + ".";

    @SneakyThrows
    public static void sendPacket(final CommandSender sender, final Object packet) {
        Object handle = sender.getClass().getMethod("getHandle").invoke(sender);
        Object connection = handle.getClass().getField("playerConnection").get(handle);
        connection.getClass().getMethod("sendPacket", getNMS("Packet")).invoke(connection, packet);
    }

    public static Class<?> getNMS(final String nms) throws ClassNotFoundException {
        return Class.forName(pathNMS + nms);
    }

    public static Class<?> getOBC(final String nms) throws ClassNotFoundException {
        return Class.forName(pathOBC + nms);
    }

    public static Class<?> getClazz(final String path) throws ClassNotFoundException {
        return Class.forName(path);
    }

    public static Class<?> getClazz(final String path, final String clazzName) throws ClassNotFoundException {
        return Class.forName(path + clazzName);
    }

    public static Constructor<?> getCon(final String nms, final Class<?>... parameterClass) throws ClassNotFoundException, NoSuchMethodException {
        return getNMS(nms).getConstructor(parameterClass);
    }

    public static Constructor<?> getCon(final Class<?> clazz, final Class<?>... parameterClass) throws NoSuchMethodException {
        return clazz.getConstructor(parameterClass);
    }

    public static Constructor<?> getDcCon(final String nms, final Class<?>... parameterClass) throws ClassNotFoundException, NoSuchMethodException {
        Constructor<?> constructor = getNMS(nms).getDeclaredConstructor(parameterClass);
        constructor.setAccessible(true);
        return constructor;
    }

    public static Constructor<?> getDcCon(final Class<?> clazz, final Class<?>... parameterClass) throws NoSuchMethodException {
        Constructor<?> constructor = clazz.getDeclaredConstructor(parameterClass);
        constructor.setAccessible(true);
        return constructor;
    }

    public static Method getMethod(final String nms, final String methodName, final Class<?>... parameterClass) throws ClassNotFoundException, NoSuchMethodException {
        return getNMS(nms).getMethod(methodName, parameterClass);
    }

    public static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterClass) throws NoSuchMethodException {
        return clazz.getMethod(methodName, parameterClass);
    }

    public static Method getDcMethod(final String nms, final String methodName, final Class<?>... parameterClass) throws ClassNotFoundException, NoSuchMethodException {
        Method method = getNMS(nms).getDeclaredMethod(methodName, parameterClass);
        method.setAccessible(true);
        return method;
    }

    public static Method getDcMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterClass) throws NoSuchMethodException {
        Method method = clazz.getDeclaredMethod(methodName, parameterClass);
        method.setAccessible(true);
        return method;
    }

    public static Field getField(final String nms, final String variableName) throws ClassNotFoundException, NoSuchFieldException {
        return getNMS(nms).getField(variableName);
    }

    public static Field getField(final Class<?> clazz, final String variableName) throws NoSuchFieldException {
        return clazz.getField(variableName);
    }

    public static Field getDcField(final String nms, final String variableName) throws ClassNotFoundException, NoSuchFieldException {
        Field field = getNMS(nms).getDeclaredField(variableName);
        field.setAccessible(true);
        return field;
    }

    public static Field getDcField(final Class<?> clazz, final String variableName) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(variableName);
        field.setAccessible(true);
        return field;
    }

    public static Object instance(final Constructor<?> constructor, final Object... args) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return constructor.newInstance(args);
    }

    public static Object invoke(final Method method, final Object instance, final Object... args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(instance, args);
    }

    public static Object invokeStatic(final Method method, final Object... args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(null, args);
    }

    public static Object get(final Field field, final Object instance) throws IllegalAccessException {
        return field.get(instance);
    }

    public static Class<?> getType(final Field field) {
        return field.getType();
    }

    public static boolean isEqualsOrMoreRecent(final int checkVersion) {
        return Integer.parseInt(version.split("_") [1]) >= checkVersion;
    }
}

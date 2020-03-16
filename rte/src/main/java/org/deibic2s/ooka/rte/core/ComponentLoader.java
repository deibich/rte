package org.deibic2s.ooka.rte.core;

import org.deibic2s.ooka.componentmodel.annotations.Start;
import org.deibic2s.ooka.componentmodel.annotations.Stop;
import org.deibic2s.ooka.componentmodel.logging.InjectLogger;
import org.deibic2s.ooka.componentmodel.logging.InjectLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import java.util.jar.Manifest;

import org.deibic2s.ooka.componentmodel.events.Event;
import org.deibic2s.ooka.componentmodel.events.InjectEvent;
import org.deibic2s.ooka.componentmodel.events.Observes;
import org.deibic2s.ooka.componentmodel.logging.ILoggerFactory;
import org.deibic2s.ooka.componentmodel.logging.ILogger;

public class ComponentLoader {

    void loadComponentFromFile(Component component) {
        File filePath = new File(component.getPathToComponent());

        if(filePath.exists() && filePath.isFile() && filePath.canRead() ) {
            if(Pattern.matches(".*\\.jar$", component.getPathToComponent())){
                ClassLoader cl = getClassLoader(component.getPathToComponent());
                component.setClassLoader(cl);
                component.rename(filePath.getName().substring(0, filePath.getName().length()-4));
            }else {
                System.out.println("File does not match");
            }
        }else {
            System.out.println("Cant access file");
        }
    }

    void setStartStopToComponent(Component component){
        ClassLoader cl = component.getClassLoader();
        if(cl == null)
            return;
        JarFile jarFile = null;
        try{
            jarFile = new JarFile(component.getPathToComponent());
            Enumeration<JarEntry> e = jarFile.entries();

            while (e.hasMoreElements()) {
                JarEntry je = e.nextElement();
                if (je.isDirectory() || !je.getName().endsWith(".class")) {
                    continue;
                }
                String className = je.getName().substring(0, je.getName().length()-6);
                className = className.replace('/', '.');

                Class<?> c = cl.loadClass(className);

                Method[] methods = c.getDeclaredMethods();
                for(Method method : methods) {
                    Annotation[] annotations = method.getDeclaredAnnotations();
                    for(Annotation annotation : annotations) {
                        if (annotation.annotationType().equals(Start.class)) {
                            component.setStartStopClass(c);
                            component.setStartMethod(method);
                        } else if (annotation.annotationType().equals(Stop.class)) {
                            component.setStartStopClass(c);
                            component.setStopMethod(method);
                        }
                    }
                }
            }
            jarFile.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    void injectLogger(Component component){
        if(component == null || !component.isStartStopSet() || component.getClassLoader() == null)
            return;

        Class<?> startStopClass = component.getStartStopClass();

        if(startStopClass == null)
            return;

        for(Field field : startStopClass.getDeclaredFields()){
            for(Annotation annotation : field.getAnnotations()){
                if(annotation.annotationType().equals(InjectLogger.class) && field.getType().equals(ILogger.class)){
                    field.setAccessible(true);
                    component.setComponentLoggerField(field);
                    break;
                }
            }
        }
    }
    
    void injectLoggerFactory(Component component){
        if(component == null || !component.isStartStopSet() || component.getClassLoader() == null)
            return;

        Class<?> startStopClass = component.getStartStopClass();

        if(startStopClass == null)
            return;

        for(Field field : startStopClass.getDeclaredFields()){
            for(Annotation annotation : field.getAnnotations()){
                if(annotation.annotationType().equals(InjectLoggerFactory.class) && field.getType().equals(ILoggerFactory.class)){
                    field.setAccessible(true);
                    
                    component.setComponentLoggerFactoryField(field);
                    break;
                }
            }
        }
    }

    void getEventListeners(Component component) {
        if (component == null || !component.isStartStopSet() || component.getClassLoader() == null)
            return;

        Class<?> startStopClass = component.getStartStopClass();

        if (startStopClass == null)
            return;

        for (Method method : startStopClass.getDeclaredMethods()) {
            if (method.getParameterCount() == 1) {
                for (Annotation annotation : method.getAnnotations()) {
                    if (annotation.annotationType().equals(Observes.class)) {
                        if(method.getParameterCount() == 1 ) {
                            Observes o = method.getDeclaredAnnotation(Observes.class);
                            Type t = method.getParameterTypes()[0];
                            method.setAccessible(true);
                            component.addEventListenerMethod(o.topic(), t, method);
                        }
                    }
                }
            }
        }
    }

    void getInjectableEventFields(Component component){
        if(component == null || !component.isStartStopSet() || component.getClassLoader() == null)
            return;

        Class<?> startStopClass = component.getStartStopClass();

        if(startStopClass == null)
            return;

        for(Field field : startStopClass.getDeclaredFields()){
            for(Annotation annotation : field.getAnnotations()){
                if(annotation.annotationType().equals(InjectEvent.class)){
                    if(field.getType().equals(Event.class)){
                        ParameterizedType parameterizedType = (ParameterizedType) field.getAnnotatedType().getType();
                        field.setAccessible(true);
                        component.addEventField(parameterizedType.getActualTypeArguments()[0], field);
                    }
                }
            }
        }
    }

    /*
    void getManifestInfos(Component component) {
        JarFile jarFile = null;
        try{
            jarFile = new JarFile(component.getPathToComponent());

            Manifest manifest = jarFile.getManifest();
            
            if(manifest == null)
                return;

            Map<String, Attributes> entries = manifest.getEntries();
            Attributes a = manifest.getMainAttributes();


        } catch(Exception e) {

        }
    }
    */

    private ClassLoader getClassLoader(String pathToJAR){
        URLClassLoader cl = null;
        try {

            URL[] urls = {new URL("jar:file:" + pathToJAR + "!/")};
            cl = URLClassLoader.newInstance(urls);
            
        } catch (MalformedURLException e) {
            // TODO: Error Handling
            e.printStackTrace();
        }

        return cl;
    }
}


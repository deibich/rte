package org.deibic2s.ooka.rte.core;

import org.deibic2s.ooka.componentmodel.annotations.InProductionScope;
import org.deibic2s.ooka.componentmodel.annotations.InspectionScope;
import org.deibic2s.ooka.componentmodel.annotations.Start;
import org.deibic2s.ooka.componentmodel.annotations.Stop;
import org.deibic2s.ooka.componentmodel.annotations.UnderInspectionScope;
import org.deibic2s.ooka.componentmodel.annotations.UnderTestScope;
import org.deibic2s.ooka.componentmodel.logging.InjectLogger;
import org.deibic2s.ooka.componentmodel.logging.InjectLoggerFactory;
import org.deibic2s.ooka.rte.SimpleRTE;
import org.deibic2s.ooka.rte.logging.RTELogCreator;

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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;


import org.deibic2s.ooka.componentmodel.events.Event;
import org.deibic2s.ooka.componentmodel.events.InjectEvent;
import org.deibic2s.ooka.componentmodel.events.Observes;
import org.deibic2s.ooka.componentmodel.logging.ILoggerFactory;
import org.deibic2s.ooka.componentmodel.logging.ILogger;

public class ComponentLoader {
    private SimpleRTE myRTE;

    public ComponentLoader(SimpleRTE myRTE){
        this.myRTE = myRTE;
    }

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
                    // Kann ich überhaupt injizieren? Abhängig vom Scope
                    List<Scope> allowedScopes = getallowedScopesForField(field);

                    if(allowedScopes.size() == 0 || allowedScopes.contains(myRTE.getScope())){
                        field.setAccessible(true);
                        component.setComponentLoggerField(field);
                    }else {

                        String possibleScopes = "";
                        for (Scope scope : allowedScopes) {
                            possibleScopes += scope.toString() + ", ";
                        }
                        RTELogCreator.getInstance().getRTELogger("main").info("Could not inject Logger for Component " + component.getId() + ". Current scope: " + myRTE.getScope() + ". Logger only possible for: " + possibleScopes);
                    }
                }
            }
        }
    }

    List<Scope> getallowedScopesForField(Field field){
        Annotation[] fieldannotations = field.getAnnotations();
        List<Scope> allowedScopes = new ArrayList<Scope>();

        for (Annotation as : fieldannotations) {
            if(as.annotationType().equals(UnderInspectionScope.class)) {
                allowedScopes.add(Scope.Under_Inspection);
            } else if(as.annotationType().equals(InProductionScope.class)) {
                allowedScopes.add(Scope.In_Production);
            } else if(as.annotationType().equals(InspectionScope.class)){
                allowedScopes.add(Scope.In_Maintenance);
            } else if(as.annotationType().equals(UnderTestScope.class)) {
                allowedScopes.add(Scope.Under_Test);
            }else {

            }
        }
        
        return allowedScopes;
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
                    List<Scope> allowedScopes = getallowedScopesForField(field);

                    if(allowedScopes.size() == 0 || allowedScopes.contains(myRTE.getScope())){
                        field.setAccessible(true);
                        component.setComponentLoggerFactoryField(field);
                    }else {
                        String possibleScopes = "";
                        for (Scope scope : allowedScopes) {
                            possibleScopes += scope.toString() + ", ";
                        }
                        RTELogCreator.getInstance().getRTELogger("main").info("Could not inject LoggerFactory for Component " + component.getId() + ". Current scope: " + myRTE.getScope() + ". LoggerFactory only possible for: " + possibleScopes);

                    }
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
                        List<Scope> allowedScopes = getallowedScopesForField(field);

                        if(allowedScopes.size() == 0 || allowedScopes.contains(myRTE.getScope())){
                            ParameterizedType parameterizedType = (ParameterizedType) field.getAnnotatedType().getType();
                            field.setAccessible(true);
                            component.addEventField(parameterizedType.getActualTypeArguments()[0], field);
                        }else {
                            String possibleScopes = "";
                            for (Scope scope : allowedScopes) {
                                possibleScopes += scope.toString() + ", ";
                            }
                            RTELogCreator.getInstance().getRTELogger("main").info("Could not inject EventListeners for Component " + component.getId() + ". Current scope: " + myRTE.getScope()+ ". Eventlistener only possible for: " + possibleScopes);
                        }
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


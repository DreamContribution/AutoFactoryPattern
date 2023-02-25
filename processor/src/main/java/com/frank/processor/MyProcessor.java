package com.frank.processor;

import com.frank.annotation.AutoFactory;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.reflect.GenericDeclaration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;


@AutoService(Processor.class)
public class MyProcessor extends AbstractProcessor {

    /**
     * 生成文件的工具类
     */
    private Filer filer;

    /**
     * 打印信息
     */
    private Messager messager;

    //元素相关
    private Elements elementUtils;

    private Types typeUtils;


    /**
     * 初始化操作，获取系统工具类
     *
     * @param processingEnv
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
    }

    /**
     * 设置支持的版本
     *
     * @return 最新版本号即可
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return super.getSupportedSourceVersion();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> set = new HashSet<>();
        set.add(AutoFactory.class.getCanonicalName());
        return set;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager.printMessage(Diagnostic.Kind.NOTE, "annotations size--->" + annotations.size());
        // 1.获取要处理的注解的元素的集合
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(AutoFactory.class);

        // process 方法会调用3次，只有一次有效Í
        if (elements == null || elements.size() < 1) {
            return true;
        }

        Map<String, FactoryInfo> map = new HashMap<>();

        // 2.按类来划分注解元素，因为每个使用注解的类都会生成相应的代理类
        for (Element element : elements) {
            TypeElement typeElement = (TypeElement) element;
            messager.printMessage(Diagnostic.Kind.NOTE, "item annotation-->" + typeElement.getQualifiedName());
            record(map, typeElement);
        }
        generateCode(map);
        return true;
    }

    void generateCode(Map<String, FactoryInfo> map) {
        if (map == null || map.isEmpty()) {
            return;
        }

        for (FactoryInfo itemFactory : map.values()) {
            generateItemFactoryCode(itemFactory);
        }
    }

    void generateItemFactoryCode(FactoryInfo itemFactory) {
        if (itemFactory.subClassInfo.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "empty factory!name:" + itemFactory.factoryName);
            return;
        }
        CodeBlock.Builder builder = startMethod();
        Iterator<ClassInfo> iterator = itemFactory.subClassInfo.iterator();
        System.out.println(itemFactory.subClassInfo.size());
        while (iterator.hasNext()) {
            ClassInfo next = iterator.next();
            addSwitchCase(builder, next.id, next.classInfo);
        }

        endMethod(builder);

        MethodSpec returns = getMethodSpec(builder, itemFactory);

        TypeSpec build = TypeSpec.classBuilder(itemFactory.factoryName).addModifiers(Modifier.PUBLIC).addMethod(returns).build();

        JavaFile build1 = JavaFile.builder("com.frank.factory", build).build();
        try {
            System.out.println(build1.toString());
            build1.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MethodSpec getMethodSpec(CodeBlock.Builder builder, FactoryInfo itemFactory) {
        MethodSpec returns = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(itemFactory.methodReturnType)
                .addParameter(String.class, "which")
                .addCode(builder.build())
                .build();
        return returns;
    }

    void record(Map<String, FactoryInfo> map, TypeElement typeElement) {
        try {
            AutoFactory annotation = typeElement.getAnnotation(AutoFactory.class);
            FactoryInfo itemFactory = map.getOrDefault(annotation.factoryName(), new FactoryInfo());
            itemFactory.factoryName = annotation.factoryName();

            ClassInfo info = new ClassInfo();
            info.factoryName = annotation.factoryName();
            info.id = annotation.id();
            info.returnType = Class.forName(getSuperClass(annotation));
            info.classInfo = typeElement.getQualifiedName().toString();
            itemFactory.subClassInfo.add(info);
            itemFactory.methodReturnType = info.returnType;
            map.put(itemFactory.factoryName, itemFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getSuperClass(AutoFactory annotation) {
        try {
            GenericDeclaration aClass = annotation.returnType();// this should throw
            System.out.println("try to get the class from annotation:" + aClass);
            return aClass.toString();
        } catch (MirroredTypeException mte) {
            // always this case...
            System.out.println("try to get the class from annotation:" + mte.getTypeMirror().toString());
            return mte.getTypeMirror().toString();
        }
    }

    private CodeBlock.Builder startMethod() {
        return CodeBlock.builder().add("switch (which){\r\t");
    }

    private void addSwitchCase(@Nonnull CodeBlock.Builder builder, String caseName, String newType) {
        System.out.println("item type---->" + newType);
        builder.addStatement("case $S:\r\t\t\treturn new $L()", caseName, newType);
    }

    private void endMethod(@Nonnull CodeBlock.Builder builder) {
        builder.addStatement("default:\r\t\t\tthrow new IllegalArgumentException(\"wrong type name\")").add("}\r\t");
    }

}
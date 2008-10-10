/*
 *  Copyright 2008 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.ibatis.ibator.generator.ibatis2;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.ibator.api.GeneratedJavaFile;
import org.apache.ibatis.ibator.api.GeneratedXmlFile;
import org.apache.ibatis.ibator.api.IntrospectedTable;
import org.apache.ibatis.ibator.api.ProgressCallback;
import org.apache.ibatis.ibator.api.dom.java.CompilationUnit;
import org.apache.ibatis.ibator.api.dom.xml.Document;
import org.apache.ibatis.ibator.generator.AbstractGenerator;
import org.apache.ibatis.ibator.generator.JavaGenerator;
import org.apache.ibatis.ibator.generator.XmlGenerator;
import org.apache.ibatis.ibator.generator.ibatis2.dao.DAOGenerator;
import org.apache.ibatis.ibator.generator.ibatis2.dao.templates.AbstractDAOTemplate;
import org.apache.ibatis.ibator.generator.ibatis2.dao.templates.GenericCIDAOTemplate;
import org.apache.ibatis.ibator.generator.ibatis2.dao.templates.GenericSIDAOTemplate;
import org.apache.ibatis.ibator.generator.ibatis2.dao.templates.IbatisDAOTemplate;
import org.apache.ibatis.ibator.generator.ibatis2.dao.templates.SpringDAOTemplate;
import org.apache.ibatis.ibator.generator.ibatis2.model.BaseRecordGenerator;
import org.apache.ibatis.ibator.generator.ibatis2.model.ExampleGenerator;
import org.apache.ibatis.ibator.generator.ibatis2.model.PrimaryKeyGenerator;
import org.apache.ibatis.ibator.generator.ibatis2.model.RecordWithBLOBsGenerator;
import org.apache.ibatis.ibator.generator.ibatis2.sqlmap.SqlMapGenerator;
import org.apache.ibatis.ibator.internal.IbatorObjectFactory;

/**
 * 
 * @author Jeff Butler
 *
 */
public class IntrospectedTableIbatis2Impl extends IntrospectedTable {
    protected List<JavaGenerator> javaModelGenerators;
    protected List<JavaGenerator> daoGenerators;
    protected XmlGenerator sqlMapGenerator;

    public IntrospectedTableIbatis2Impl() {
        super();
        javaModelGenerators = new ArrayList<JavaGenerator>();
        daoGenerators = new ArrayList<JavaGenerator>();
    }

    @Override
    public void calculateGenerators(List<String> warnings, ProgressCallback progressCallback) {
        calculateJavaModelGenerators(warnings, progressCallback);
        calculateDAOGenerators(warnings, progressCallback);
        calculateSqlMapGenerator(warnings, progressCallback);
    }
    
    protected void calculateSqlMapGenerator(List<String> warnings, ProgressCallback progressCallback) {
        sqlMapGenerator = new SqlMapGenerator();
        initializeAbstractGenerator(sqlMapGenerator, warnings, progressCallback);
    }
    
    protected void calculateDAOGenerators(List<String> warnings, ProgressCallback progressCallback) {
        if (ibatorContext.getDaoGeneratorConfiguration() == null) {
            return;
        }
        
        String type = ibatorContext.getDaoGeneratorConfiguration().getConfigurationType();
        
        AbstractDAOTemplate abstractDAOTemplate;
        if ("IBATIS".equalsIgnoreCase(type)) {
            abstractDAOTemplate = new IbatisDAOTemplate();
        } else if ("SPRING".equalsIgnoreCase(type)) {
            abstractDAOTemplate = new SpringDAOTemplate();
        } else if ("GENERIC-CI".equalsIgnoreCase(type)) {
            abstractDAOTemplate = new GenericCIDAOTemplate();
        } else if ("GENERIC-SI".equalsIgnoreCase(type)) {
            abstractDAOTemplate = new GenericSIDAOTemplate();
        } else {
            // TODO - document this as a way to supply custom template for DAOs
            abstractDAOTemplate = (AbstractDAOTemplate) IbatorObjectFactory.createObject(type);
        }

        boolean generateForJava5 = "Java5".equalsIgnoreCase(ibatorContext.getTargetJRE());
        JavaGenerator javaGenerator = new DAOGenerator(abstractDAOTemplate, generateForJava5);
        initializeAbstractGenerator(javaGenerator, warnings, progressCallback);
        daoGenerators.add(javaGenerator);
    }
    
    protected void calculateJavaModelGenerators(List<String> warnings, ProgressCallback progressCallback) {
        boolean generateForJava5 = "Java5".equalsIgnoreCase(ibatorContext.getTargetJRE());
        
        if (getRules().generateExampleClass()) {
            JavaGenerator javaGenerator = new ExampleGenerator(generateForJava5);
            initializeAbstractGenerator(javaGenerator, warnings, progressCallback);
            javaModelGenerators.add(javaGenerator);
        }
        
        if (getRules().generatePrimaryKeyClass()) {
            JavaGenerator javaGenerator = new PrimaryKeyGenerator();
            initializeAbstractGenerator(javaGenerator, warnings, progressCallback);
            javaModelGenerators.add(javaGenerator);
        }
        
        if (getRules().generateBaseRecordClass()) {
            JavaGenerator javaGenerator = new BaseRecordGenerator();
            initializeAbstractGenerator(javaGenerator, warnings, progressCallback);
            javaModelGenerators.add(javaGenerator);
        }
        
        if (getRules().generateRecordWithBLOBsClass()) {
            JavaGenerator javaGenerator = new RecordWithBLOBsGenerator();
            initializeAbstractGenerator(javaGenerator, warnings, progressCallback);
            javaModelGenerators.add(javaGenerator);
        }
    }
    
    protected void initializeAbstractGenerator(AbstractGenerator abstractGenerator, List<String> warnings, ProgressCallback progressCallback) {
        abstractGenerator.setIbatorContext(ibatorContext);
        abstractGenerator.setIntrospectedTable(this);
        abstractGenerator.setProgressCallback(progressCallback);
        abstractGenerator.setWarnings(warnings);
    }
    
    @Override
    public List<GeneratedJavaFile> getGeneratedJavaFiles() {
        List<GeneratedJavaFile> answer = new ArrayList<GeneratedJavaFile>();
        
        for (JavaGenerator javaGenerator : javaModelGenerators) {
            List<CompilationUnit> compilationUnits = javaGenerator.getCompilationUnits();
            for (CompilationUnit compilationUnit : compilationUnits) {
                GeneratedJavaFile gjf = new GeneratedJavaFile(compilationUnit, ibatorContext.getJavaModelGeneratorConfiguration().getTargetProject());
                answer.add(gjf);
            }
        }
        
        for (JavaGenerator javaGenerator : daoGenerators) {
            List<CompilationUnit> compilationUnits = javaGenerator.getCompilationUnits();
            for (CompilationUnit compilationUnit : compilationUnits) {
                GeneratedJavaFile gjf = new GeneratedJavaFile(compilationUnit, ibatorContext.getDaoGeneratorConfiguration().getTargetProject());
                answer.add(gjf);
            }
        }
        
        return answer;
    }

    @Override
    public List<GeneratedXmlFile> getGeneratedXmlFiles() {
        List<GeneratedXmlFile> answer = new ArrayList<GeneratedXmlFile>();
        
        Document document = sqlMapGenerator.getDocument();
        GeneratedXmlFile gxf = new GeneratedXmlFile(document,
            getSqlMapFileName(),
            getSqlMapPackage(),
            ibatorContext.getSqlMapGeneratorConfiguration().getTargetProject(),
            true);
        if (ibatorContext.getPlugins().sqlMapGenerated(gxf, this)) {
            answer.add(gxf);
        }
        
        return answer;
    }

    @Override
    public int getNumberOfSubtasks() {
        int answer = 0;
        
        for (JavaGenerator javaGenerator : javaModelGenerators) {
            answer += javaGenerator.getNumberOfSubtasks();
        }
        
        for (JavaGenerator javaGenerator : daoGenerators) {
            answer += javaGenerator.getNumberOfSubtasks();
        }
        
        answer += sqlMapGenerator.getNumberOfSubtasks();
        
        return answer;
    }
}

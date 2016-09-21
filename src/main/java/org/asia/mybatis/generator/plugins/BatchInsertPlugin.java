/**
 * 
 */
package org.asia.mybatis.generator.plugins;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.IntrospectedTable.TargetRuntime;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.GeneratedKey;

/**
 * @author yugn
 * 
 * 插件，在生成的代码中，增加批量写入的功能； 2014.2.11 ；
 * 仅对 mybatis 有效；
 */
public class BatchInsertPlugin extends PluginAdapter {
	
	private final static String METHOD_NAME = "insertBatch";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mybatis.generator.api.Plugin#validate(java.util.List)
	 */
	@Override
	public boolean validate(List<String> warnings) {
		// TODO Auto-generated method stub
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mybatis.generator.api.PluginAdapter#clientGenerated(org.mybatis.generator.api.dom.java.Interface,
	 *      org.mybatis.generator.api.dom.java.TopLevelClass,
	 *      org.mybatis.generator.api.IntrospectedTable)
	 */
	@Override
	public boolean clientGenerated(Interface interfaze,
			TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		// TODO Auto-generated method stub

		if (introspectedTable.getTargetRuntime() == TargetRuntime.MYBATIS3) {
			addInsertMethod(interfaze,introspectedTable);
		}

		return super.clientGenerated(interfaze, topLevelClass,
				introspectedTable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mybatis.generator.api.PluginAdapter#sqlMapDocumentGenerated(org.mybatis.generator.api.dom.xml.Document,
	 *      org.mybatis.generator.api.IntrospectedTable)
	 */
	@Override
	public boolean sqlMapDocumentGenerated(Document document,
			IntrospectedTable introspectedTable) {
		// TODO Auto-generated method stub
		if(introspectedTable.getTargetRuntime() == TargetRuntime.MYBATIS3){
			addElements(document.getRootElement(),introspectedTable);
		}
		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}
	
	
	/**
	 * SQL-Mapping 文件中增加的 id为 insertBatch的批量写入
	 * 
	 * <insert id="addTrainRecordBatch" useGeneratedKeys="true" parameterType="java.util.List">  
    <selectKey resultType="long" keyProperty="id" order="AFTER">  
        SELECT  
        LAST_INSERT_ID()  
    </selectKey>  
    insert into t_train_record (add_time,emp_id,activity_id,flag)   
    values  
    <foreach collection="list" item="item" index="index" separator="," >  
        (#{item.addTime},#{item.empId},#{item.activityId},#{item.flag})  
    </foreach>  
</insert>  
	 * @param parentElement
	 * @param introspectedTable
	 */
	private void addElements(XmlElement parentElement,IntrospectedTable introspectedTable) {
		XmlElement answer = new XmlElement("insert"); //$NON-NLS-1$

        answer.addAttribute(new Attribute(
                "id", METHOD_NAME)); //$NON-NLS-1$

        FullyQualifiedJavaType parameterType;
        /*if (isSimple) {
            parameterType = new FullyQualifiedJavaType(
                    introspectedTable.getBaseRecordType());
        } else {*/
            parameterType = introspectedTable.getRules()
                    .calculateAllFieldsClass();
        /*}*/

        answer.addAttribute(new Attribute("parameterType", //$NON-NLS-1$
                parameterType.getFullyQualifiedName()));

        context.getCommentGenerator().addComment(answer);

        GeneratedKey gk = introspectedTable.getGeneratedKey();
        if (gk != null) {
            IntrospectedColumn introspectedColumn = introspectedTable
                    .getColumn(gk.getColumn());
            // if the column is null, then it's a configuration error. The
            // warning has already been reported
            if (introspectedColumn != null) {
                if (gk.isJdbcStandard()) {
                    answer.addAttribute(new Attribute(
                            "useGeneratedKeys", "true")); //$NON-NLS-1$ //$NON-NLS-2$
                    answer.addAttribute(new Attribute(
                            "keyProperty", introspectedColumn.getJavaProperty())); //$NON-NLS-1$
                } else {
                    answer.addElement(getSelectKey(introspectedColumn, gk));
                }
            }
        }

        StringBuilder insertClause = new StringBuilder();
        StringBuilder valuesClause = new StringBuilder();

        insertClause.append("insert into "); //$NON-NLS-1$
        insertClause.append(introspectedTable
                .getFullyQualifiedTableNameAtRuntime());
        insertClause.append(" ("); //$NON-NLS-1$

        valuesClause.append("values <foreach collection=\"list\" item=\"item\" index=\"index\" separator=\",\" > ("); //$NON-NLS-1$
          
        //(#{item.addTime},#{item.empId},#{item.activityId},#{item.flag})  
    
        List<String> valuesClauses = new ArrayList<String>();
        Iterator<IntrospectedColumn> iter = introspectedTable.getAllColumns()
                .iterator();
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();
            if (introspectedColumn.isIdentity()) {
                // cannot set values on identity fields
                continue;
            }

            insertClause.append(MyBatis3FormattingUtilities
                    .getEscapedColumnName(introspectedColumn));
            
            // 批量插入,如果是sequence字段,则插入不需要item.前缀
            if(introspectedColumn.isSequenceColumn()) {
            	valuesClause.append(MyBatis3FormattingUtilities
            			.getParameterClause(introspectedColumn));
            } else {
            	valuesClause.append(MyBatis3FormattingUtilities
            			.getParameterClause(introspectedColumn,"item."));
            }
            if (iter.hasNext()) {
                insertClause.append(", "); //$NON-NLS-1$
                valuesClause.append(", "); //$NON-NLS-1$
            }

            if (valuesClause.length() > 80) {
                answer.addElement(new TextElement(insertClause.toString()));
                insertClause.setLength(0);
                OutputUtilities.xmlIndent(insertClause, 1);

                valuesClauses.add(valuesClause.toString());
                valuesClause.setLength(0);
                OutputUtilities.xmlIndent(valuesClause, 1);
            }
        }

        insertClause.append(')');
        answer.addElement(new TextElement(insertClause.toString()));

        valuesClause.append(")</foreach>");
        valuesClauses.add(valuesClause.toString());

        for (String clause : valuesClauses) {
            answer.addElement(new TextElement(clause));
        }
        
        parentElement.addElement(answer);
        /*
        if (context.getPlugins().sqlMapInsertElementGenerated(answer,
                introspectedTable)) {
            parentElement.addElement(answer);
        }*/
    }
	
	/**
     * This method should return an XmlElement for the select key used to
     * automatically generate keys.
     * 
     * @param introspectedColumn
     *            the column related to the select key statement
     * @param generatedKey
     *            the generated key for the current table
     * @return the selectKey element
     */
    protected XmlElement getSelectKey(IntrospectedColumn introspectedColumn,
            GeneratedKey generatedKey) {
        String identityColumnType = introspectedColumn
                .getFullyQualifiedJavaType().getFullyQualifiedName();

        XmlElement answer = new XmlElement("selectKey"); //$NON-NLS-1$
        answer.addAttribute(new Attribute("resultType", identityColumnType)); //$NON-NLS-1$
        answer.addAttribute(new Attribute(
                "keyProperty", introspectedColumn.getJavaProperty())); //$NON-NLS-1$
        answer.addAttribute(new Attribute("order", //$NON-NLS-1$
                generatedKey.getMyBatis3Order())); 
        
        answer.addElement(new TextElement(generatedKey
                        .getRuntimeSqlStatement()));

        return answer;
    }
    
    
    private void addInsertMethod(Interface interfaze, IntrospectedTable introspectedTable){
    	Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        Method method = new Method();

        method.setReturnType(new FullyQualifiedJavaType("void"));
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName(METHOD_NAME);

        FullyQualifiedJavaType parameterType;
       /* if (isSimple) {
            parameterType = new FullyQualifiedJavaType(
                    introspectedTable.getBaseRecordType());
        } else {*/
            parameterType = introspectedTable.getRules()
                    .calculateAllFieldsClass();
        //}

        importedTypes.add(parameterType);
        
        FullyQualifiedJavaType listParamType = new FullyQualifiedJavaType("java.util.List<"+parameterType+">");
        
        method.addParameter(new Parameter(listParamType, "recordLst")); //$NON-NLS-1$

        context.getCommentGenerator().addGeneralMethodComment(method,
                introspectedTable);

       // addMapperAnnotations(interfaze, method);
        interfaze.addImportedTypes(importedTypes);
        interfaze.addMethod(method);
        /*if (context.getPlugins().clientInsertMethodGenerated(method, interfaze,
                introspectedTable)) {
            interfaze.addImportedTypes(importedTypes);
            interfaze.addMethod(method);
        }*/
    }
}

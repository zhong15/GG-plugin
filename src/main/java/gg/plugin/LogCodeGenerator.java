/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gg.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gg.plugin.utils.Utils;
import org.jetbrains.annotations.NotNull;

/**
 * @author Zhong
 * @since 1.0
 */
public class LogCodeGenerator extends AnAction {

    private static final Logger log = Logger.getInstance(LogCodeGenerator.class);
    private static final String SLF4J_LOGGER = "org.slf4j.Logger";
    private static final String LOG4J_LOGGER = "org.apache.log4j.Logger";
    private static final String JAVA_LOGGER = "java.util.logging.Logger";

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(Utils.isMethod(e));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        log.info("start");
        WriteCommandAction.runWriteCommandAction(e.getProject(), () -> {
            PsiMethod psiMethod = Utils.getPsiMethod(e);
            if (psiMethod == null) {
                log.info("psiMethod is null");
                return;
            }

            log.info("psiMethod: " + psiMethod.getName());

            PsiParameterList psiParameterList = psiMethod.getParameterList();
            if (psiParameterList == null) {
                log.info("psiParameterList is null");
                return;
            }

            PsiParameter[] psiParameters = psiParameterList.getParameters();
            if (Utils.isEmptyArray(psiParameters)) {
                log.info("psiParameters is empty");
                return;
            }

            List<String> list = new ArrayList<>();
            for (PsiParameter p : psiParameters) {
                list.add(p.getName());
            }

            PsiClass psiClass = Utils.getPsiClass(psiMethod);
            if (psiClass == null) {
                log.info("psiClass is null");
                return;
            }
            PsiField logField = getLogField(psiClass);
            if (logField == null) {
                log.info("logField not found");
            }

            String logCode = Utils.getLogCode(logField == null ? null : logField.getName(), list);
            log.info("logCode: " + logCode);
            if (logCode == null || logCode.trim().length() == 0) {
                log.info("logCode is empty");
                return;
            }

            boolean comment = logField == null || logField.getType() == null || logField.getType()
                    .equalsToText(JAVA_LOGGER);

            PsiElementFactory psiElementFactory = PsiElementFactory.getInstance(e.getProject());
            if (psiElementFactory == null) {
                log.info("psiElementFactory is null");
                return;
            }

            PsiCodeBlock psiCodeBlock = psiMethod.getBody();
            PsiElement prePsiElement = psiCodeBlock.getFirstBodyElement();
            if (comment) {
                for (String s : logCode.split("\n")) {
                    s = "// " + s;
                    PsiComment psiComment = psiElementFactory.createCommentFromText(s, null);
                    prePsiElement = psiCodeBlock.addAfter(psiComment, prePsiElement);
                }
            } else {
                PsiStatement psiStatement = psiElementFactory.createStatementFromText(logCode, null);
                psiCodeBlock.addAfter(psiStatement, prePsiElement);
            }
        });
    }

    private static PsiField getLogField(PsiClass psiClass) {
        if (Utils.isEmptyArray(psiClass.getFields())) {
            log.info("psiClass fields is empty");
            return null;
        }

        for (String e : Arrays.asList(SLF4J_LOGGER, LOG4J_LOGGER, JAVA_LOGGER)) {
            for (PsiField f : psiClass.getFields()) {
                if (f.getType() == null) {
                    continue;
                }
                if (f.getType().equalsToText(e)) {
                    return f;
                }
            }
        }

        return null;
    }
}

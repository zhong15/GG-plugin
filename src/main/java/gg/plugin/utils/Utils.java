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

package gg.plugin.utils;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationParameterList;
import com.intellij.psi.PsiArrayInitializerMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiKeyword;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.util.PsiTreeUtil;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Zhong
 * @since 1.0
 */
public class Utils {

    private Utils() {
    }

    public static boolean isClass(AnActionEvent e) {
        return getPsiClassOrPsiFilePublicPsiClass(e) != null;
    }

    public static boolean isMethod(AnActionEvent e) {
        return getPsiMethod(e) != null;
    }

    public static PsiMethod getPsiMethod(AnActionEvent e) {
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) {
            return null;
        }

        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return null;
        }

        int offset = editor.getCaretModel().getOffset();
        PsiElement psiElement = psiFile.findElementAt(offset);
        if (psiElement == null) {
            return null;
        }

        if (psiElement instanceof PsiMethod) {
            return (PsiMethod) psiElement;
        }

        return PsiTreeUtil.getParentOfType(psiElement, PsiMethod.class);
    }

    public static String getLogCode(String logField, List<String> list) {
        if (list == null || list.size() == 0) {
            return null;
        }
        if (logField == null) {
            logField = "log";
        }
        String logCode = logField + ".info(\"";
        for (String e : list) {
            logCode += e + ": {}, ";
        }
        logCode = logCode.substring(0, logCode.length() - 2);
        if (list.size() <= 5) {
            logCode += "\", ";
        } else {
            logCode += "\",\n";
        }
        for (String e : list) {
            logCode += e + ", ";
        }
        logCode = logCode.substring(0, logCode.length() - 2);
        logCode += ");";
        return logCode;
    }

    public static List<String> listAnnAttrValue(PsiAnnotation[] psiAnnotations,
                                                Predicate<String> annFilter,
                                                Predicate<String> attrFilter) {
        if (Utils.isEmptyArray(psiAnnotations)) {
            return null;
        }

        List<String> list = new ArrayList<>();

        for (PsiAnnotation a : psiAnnotations) {
            if (a == null) {
                continue;
            }

            if (!annFilter.test(a.getQualifiedName())) {
                continue;
            }

            PsiAnnotationParameterList apList = a.getParameterList();
            for (PsiNameValuePair nv : apList.getAttributes()) {
                if (!attrFilter.test(nv.getName())) {
                    continue;
                }
//        if (!(nv.getName() == null
//            || Objects.equals("value", nv.getName())
//            || Objects.equals("path", nv.getName()))) {
//          continue;
//        }
                PsiLiteralExpression singleValue = PsiTreeUtil.getChildOfType(nv,
                        PsiLiteralExpression.class);
                if (singleValue != null) {
//          list.add(singleValue.getText());
                    list.add(singleValue.getValue().toString());
                }

                PsiArrayInitializerMemberValue arrayValue = PsiTreeUtil.getChildOfType(nv,
                        PsiArrayInitializerMemberValue.class);
                if (arrayValue != null) {
                    PsiLiteralExpression[] array = PsiTreeUtil.getChildrenOfType(arrayValue,
                            PsiLiteralExpression.class);
                    if (array != null) {
                        for (PsiLiteralExpression value : array) {
                            if (value != null) {
//                list.add(value.getText());
                                list.add(value.getValue().toString());
                            }
                        }
                    }
                }
            }
        }

        return list;
    }

    public static boolean isStaticOrFinal(PsiField psiField) {
        if (psiField == null) {
            return false;
        }
        PsiModifierList psiModifierList = psiField.getModifierList();
        if (psiModifierList == null) {
            return false;
        }
        PsiElement[] psiElements = psiModifierList.getChildren();
        if (Utils.isEmptyArray(psiElements)) {
            return false;
        }
        for (PsiElement e : psiElements) {
            if (e instanceof PsiKeyword) {
                PsiKeyword k = (PsiKeyword) e;
                if (Objects.equals(k.getText(), "static")
                        || Objects.equals(k.getText(), "final")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static PsiClass getPsiClass(PsiMethod psiMethod) {
        return PsiTreeUtil.getParentOfType(psiMethod, PsiClass.class);
    }

    public static PsiClass getPsiClassOrPsiFilePublicPsiClass(AnActionEvent e) {
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) {
            return null;
        }

        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return null;
        }

        int offset = editor.getCaretModel().getOffset();
        PsiElement psiElement = psiFile.findElementAt(offset);
        if (psiElement == null) {
            return null;
        }

        if (psiElement instanceof PsiClass) {
            return (PsiClass) psiElement;
        }

        PsiClass psiClass = PsiTreeUtil.getParentOfType(psiElement, PsiClass.class);
        if (psiClass != null) {
            return psiClass;
        }

        PsiClass[] psiClasses = PsiTreeUtil.getChildrenOfType(psiFile, PsiClass.class);
        if (Utils.isEmptyArray(psiClasses)) {
            return null;
        }
        if (psiClasses.length == 1) {
            return psiClasses[0];
        }

        for (PsiClass x : psiClasses) {
            if (x == null) {
                continue;
            }
            PsiModifierList psiModifierList = x.getModifierList();
            if (psiModifierList == null) {
                continue;
            }
            if (Utils.isEmptyArray(psiModifierList.getChildren())) {
                continue;
            }
            for (PsiElement y : psiModifierList.getChildren()) {
                if (y == null) {
                    continue;
                }
                if (y instanceof PsiKeyword) {
                    if (Objects.equals(((PsiKeyword) y).getText(), "public")) {
                        return x;
                    }
                }
            }
        }

        return null;
    }

    public static void clipboard(String s) {
        if (s == null || s.length() == 0) {
            return;
        }
        StringSelection stringSelection = new StringSelection(s);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    public static <T> boolean isEmptyArray(T[] array) {
        return array == null || array.length == 0;
    }

    public static void main(String[] args) {
        System.out.println(getLogCode(null, null));
        System.out.println(getLogCode(null, Arrays.asList("a")));
        System.out.println(getLogCode(null, Arrays.asList("a", "b")));
        System.out.println(getLogCode(null, Arrays.asList("a", "b", "c")));
        System.out.println(getLogCode(null, Arrays.asList("a", "b", "c", "e")));
        System.out.println(getLogCode(null, Arrays.asList("a", "b", "c", "e", "f")));
        System.out.println(getLogCode(null, Arrays.asList("a", "b", "c", "e", "f", "g")));
        System.out.println(getLogCode(null, Arrays.asList("a", "b", "c", "e", "f", "g", "h")));
        System.out.println(getLogCode(null, Arrays.asList("a", "b", "c", "e", "f", "g", "h", "i")));
        System.out.println(
                getLogCode(null, Arrays.asList("a", "b", "c", "e", "f", "g", "h", "i", "j")));
    }
}
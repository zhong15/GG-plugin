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
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import org.jetbrains.annotations.NotNull;
import gg.plugin.utils.Utils;

/**
 * @author Zhong
 * @since 1.0
 */
public class CopyControllerParam extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(Utils.isClass(e));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiClass psiClass = Utils.getPsiClassOrPsiFilePublicPsiClass(e);
        if (psiClass == null) {
            return;
        }
        PsiField[] psiFields = psiClass.getFields();
        if (Utils.isEmptyArray(psiFields)) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (PsiField x : psiFields) {
            if (Utils.isStaticOrFinal(x)) {
                continue;
            }
            sb.append("    \"").append(x.getName()).append("\": null,\n");
        }
        if (sb.length() != 0) {
            sb.insert(0, "{\n");
            sb.setLength(sb.length() - 2);
            sb.append("\n");
            sb.append("}");
        }
        Utils.clipboard(sb.toString());
    }
}

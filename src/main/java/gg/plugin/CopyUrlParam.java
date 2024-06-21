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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import gg.plugin.utils.Utils;

/**
 * @author Zhong
 * @since 1.0
 */
public class CopyUrlParam extends AnAction {

    private static final Logger log = Logger.getInstance(CopyUrlParam.class);
    private static final String REQUEST_PARAM = "com.abc.RequestParam";

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(Utils.isMethod(e));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
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

        StringBuilder sb = new StringBuilder();

        for (PsiParameter p : psiParameters) {
            List<String> xxx = Utils.listAnnAttrValue(p.getAnnotations(),
                    s -> Objects.equals(s, REQUEST_PARAM),
                    s -> s == null || Objects.equals(s, "name") || Objects.equals(s, "value"));
            String n = p.getName();
            if (xxx != null && xxx.size() != 0 && xxx.get(0) != null
                    && xxx.get(0).trim().length() != 0) {
                n = xxx.get(0).trim();
            }
            sb.append(n).append("=").append("&");
        }

        if (sb.length() == 0) {
            return;
        }
        sb.setLength(sb.length() - 1);
        log.info("url param: " + sb);
        Utils.clipboard(sb.toString());
        log.info("复制成功");
    }
}

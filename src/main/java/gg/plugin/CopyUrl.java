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
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import gg.plugin.utils.Utils;

/**
 * @author Zhong
 * @since 1.0
 */
public class CopyUrl extends AnAction {

    private static final Logger log = Logger.getInstance(CopyUrl.class);
    private static final List<String> mappingList = Arrays.asList(
            "RequestMapping",
            "org.spring.RequestMapping",
            "GetMapping",
            "PostMapping",
            "PutMapping",
            "DeleteMapping",
            "PatchMapping"
            , "Hello"
    );

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

        PsiClass psiClass = PsiTreeUtil.getParentOfType(psiMethod, PsiClass.class);
        if (psiClass == null) {
            log.info("psiClass is null");
            return;
        }
        log.info("psiClass: " + psiClass.getName());

        List<String> prefixUrlList = Utils.listAnnAttrValue(psiClass.getAnnotations(),
                s -> mappingList.contains(s),
                s -> s == null || Objects.equals("value", s) || Objects.equals("path", s));
        if (prefixUrlList == null || prefixUrlList.size() == 0) {
            prefixUrlList = Arrays.asList("");
        }

        List<String> suffixUrlList = Utils.listAnnAttrValue(psiMethod.getAnnotations(),
                s -> mappingList.contains(s),
                s -> s == null || Objects.equals("value", s) || Objects.equals("path", s));
        if (suffixUrlList == null || suffixUrlList.size() == 0) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (String p : prefixUrlList) {
            if (p == null) {
                p = "/";
            } else {
                p = p.trim();
                if (p.endsWith("/")) {
                    p = p.substring(0, p.length() - 1);
                }
                if (!p.startsWith("/")) {
                    p = "/" + p;
                }
            }
            for (String s : suffixUrlList) {
                if (s == null || s.trim().length() == 0) {
                    continue;
                }
                s = s.trim();
                if (!s.startsWith("/")) {
                    s = "/" + s;
                }
                log.info("url: " + p + s);
                sb.append(p).append(s).append("\n");
            }
        }
        if (sb.length() == 0) {
            log.info("没有 url");
            return;
        }

        if (sb.length() != 0) {
            sb.setLength(sb.length() - 1);
        }
        log.info("url: " + sb);
        Utils.clipboard(sb.toString());
        log.info("复制成功");
    }
}

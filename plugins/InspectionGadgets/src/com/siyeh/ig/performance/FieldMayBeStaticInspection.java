package com.siyeh.ig.performance;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.FieldInspection;
import com.siyeh.ig.GroupNames;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.psiutils.ClassUtils;
import com.siyeh.ig.psiutils.SideEffectChecker;
import org.jetbrains.annotations.NotNull;

public class FieldMayBeStaticInspection extends FieldInspection{
    private final MakeStaticFix fix = new MakeStaticFix();

    public String getDisplayName(){
        return "Field may be 'static'";
    }

    public String getGroupDisplayName(){
        return GroupNames.PERFORMANCE_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location){
        return "Field #ref may be 'static' #loc";
    }

    public BaseInspectionVisitor buildVisitor(){
        return new FieldMayBeStaticVisitor();
    }

    public InspectionGadgetsFix buildFix(PsiElement location){
        return fix;
    }

    private static class MakeStaticFix extends InspectionGadgetsFix{
        public String getName(){
            return "Make static";
        }

        public void doFix(Project project, ProblemDescriptor descriptor)
                                                                         throws IncorrectOperationException{
            final PsiJavaToken m_fieldNameToken =
                    (PsiJavaToken) descriptor.getPsiElement();
                final PsiField field = (PsiField) m_fieldNameToken.getParent();
                final PsiModifierList modifiers = field.getModifierList();
                modifiers.setModifierProperty(PsiModifier.STATIC, true);
        }
    }

    private static class FieldMayBeStaticVisitor extends BaseInspectionVisitor{

        public void visitField(@NotNull PsiField field){
            if(field.hasModifierProperty(PsiModifier.STATIC)){
                return;
            }
            if(!field.hasModifierProperty(PsiModifier.FINAL)){
                return;
            }
            final PsiExpression initializer = field.getInitializer();
            if(initializer == null){
                return;
            }
            if(SideEffectChecker.mayHaveSideEffects(initializer)){
                return;
            }
            if(!canBeStatic(initializer)){
                return;
            }
            final PsiType type = field.getType();
            if(type == null){
                return;
            }

            if(!ClassUtils.isImmutable(type)){
                return;
            }
            PsiClass containingClass = field.getContainingClass();
            if (containingClass != null
                && !containingClass.hasModifierProperty(PsiModifier.STATIC)
                && containingClass.getContainingClass() != null
                && !PsiUtil.isCompileTimeConstant(field)) {
              // inner class cannot have static declarations
              return;
            }
            registerFieldError(field);
        }

        private static boolean canBeStatic(PsiExpression initializer){
            final CanBeStaticVisitor canBeStaticVisitor =
                    new CanBeStaticVisitor();
            initializer.accept(canBeStaticVisitor);
            return canBeStaticVisitor.canBeStatic();
        }
    }
}

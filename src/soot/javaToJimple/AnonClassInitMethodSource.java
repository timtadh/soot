package soot.javaToJimple;

import java.util.*;
import soot.*;

public class AnonClassInitMethodSource implements soot.MethodSource {

    public soot.Body getBody(soot.SootMethod sootMethod, String phaseName){
        System.out.println("getting method: "+sootMethod.getName()+" for class: "+sootMethod.getDeclaringClass());            
        soot.Body body = soot.jimple.Jimple.v().newBody(sootMethod);

        // this formal needed
        soot.RefType type = sootMethod.getDeclaringClass().getType();
        soot.Local thisLocal = soot.jimple.Jimple.v().newLocal("this", type);
        body.getLocals().add(thisLocal);

        soot.jimple.ThisRef thisRef = soot.jimple.Jimple.v().newThisRef(type);

        soot.jimple.Stmt thisStmt = soot.jimple.Jimple.v().newIdentityStmt(thisLocal, thisRef);
        body.getUnits().add(thisStmt);
       
        ArrayList invokeList = new ArrayList();
        ArrayList invokeTypeList = new ArrayList();
        soot.Local outerLocal = null;
        
        // param
        Iterator fIt = sootMethod.getParameterTypes().iterator();
        int counter = 0;
        while (fIt.hasNext()){
            soot.Type fType = (soot.Type)fIt.next();
            soot.Local local = soot.jimple.Jimple.v().newLocal("r"+counter, fType);
            body.getLocals().add(local);
            soot.jimple.ParameterRef paramRef = soot.jimple.Jimple.v().newParameterRef(fType, counter);
            soot.jimple.Stmt stmt = soot.jimple.Jimple.v().newIdentityStmt(local, paramRef);
            if (!fType.equals(outerClassType)){
                invokeTypeList.add(fType);
                invokeList.add(local);
            }    
            else {
                outerLocal = local;   
            }
            body.getUnits().add(stmt);
        }
        /*soot.Type sootType = sootMethod.getParameterType(0);
        soot.Local formalLocal = soot.jimple.Jimple.v().newLocal("r0", sootType);
       
        body.getLocals().add(formalLocal);
        
        soot.jimple.ParameterRef paramRef = soot.jimple.Jimple.v().newParameterRef(sootType, 0);
        soot.jimple.Stmt stmt = soot.jimple.Jimple.v().newIdentityStmt(formalLocal, paramRef);
        body.getUnits().add(stmt);*/
                   
        // invoke
        soot.SootMethod callMethod = sootMethod.getDeclaringClass().getSuperclass().getMethod("<init>",  invokeTypeList, soot.VoidType.v());
        soot.jimple.InvokeExpr invoke = soot.jimple.Jimple.v().newSpecialInvokeExpr(thisLocal, callMethod, invokeList);

        soot.jimple.Stmt invokeStmt = soot.jimple.Jimple.v().newInvokeStmt(invoke);
        body.getUnits().add(invokeStmt);
        
        // field assign
        soot.SootField field = sootMethod.getDeclaringClass().getField("this$0", outerClassType);
        soot.jimple.InstanceFieldRef ref = soot.jimple.Jimple.v().newInstanceFieldRef(thisLocal, field);
        soot.jimple.AssignStmt assign = soot.jimple.Jimple.v().newAssignStmt(ref, outerLocal);
        body.getUnits().add(assign);
       

        // return
        soot.jimple.ReturnVoidStmt retStmt = soot.jimple.Jimple.v().newReturnVoidStmt();
        body.getUnits().add(retStmt);
        PackManager.v().getTransform("jb.ne").apply(body);
        
        PackManager.v().getPack("jb").apply(body);
    
        return body;
    }
    
    private soot.Type outerClassType;

    public soot.Type outerClassType(){
        return outerClassType;
    }
    
    public void outerClassType(soot.Type type){
        outerClassType = type;
    }
}
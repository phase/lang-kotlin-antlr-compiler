package xyz.jadonfowler.compiler

import org.junit.Test
import xyz.jadonfowler.compiler.ast.*
import xyz.jadonfowler.compiler.pass.PrintPass
import xyz.jadonfowler.compiler.pass.TypePass
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class TypeCheckingTest {

    @Test fun inferGlobalVariableTypes() {
        val code = """
        let a = 7
        """
        val module = compileString("inferGlobalVariableTypes", code)
        TypePass(module)

        assertEquals(T_INT32, module.globalVariables[0].type)

        println(PrintPass(module).output)
    }

    @Test fun inferLocalVariableTypes() {
        val code = """
        test (a, b, c : Int) : Int
            let d = a + b,
            let e = "test",
            let f = true,
            7 + c
        """
        val module = compileString("inferLocalVariableTypes", code)
        TypePass(module)

        assertEquals(3, module.globalFunctions[0].statements.size)
        val statements = module.globalFunctions[0].statements
        assertEquals(T_INT32, (statements[0] as VariableDeclarationStatement).variable.type)
        assertEquals(T_STRING, (statements[1] as VariableDeclarationStatement).variable.type)
        assertEquals(T_BOOL, (statements[2] as VariableDeclarationStatement).variable.type)

        println(PrintPass(module).output)
    }

    @Test fun inferFunctionReturnType() {
        val code = """
        test (a, b, c : Int)
            let d = a + b,
            let e = b * d - a + b,
            let f = e / b * d - a + b + c * c,
            7 + f
        """
        val module = compileString("inferFunctionReturnType", code)
        TypePass(module)

        assertEquals(T_INT32, module.globalFunctions[0].returnType)

        println(PrintPass(module).output)
    }

    @Test fun incorrectTypeSigOnGlobalVariable() {
        val code = """
        let a : String = 3
        """
        val module = compileString("incorrectTypeSigOnGlobalVariable", code)

        TypePass(module)
        assertTrue(module.errors.size > 0)
        module.errors.forEach(::println)

        println(PrintPass(module).output)
    }

    @Test fun incorrectTypeSigOnLocalVariable() {
        val code = """
        test () : Int
            let a : Void = "test",
            6
        """
        val module = compileString("incorrectTypeSigOnLocalVariable", code)

        TypePass(module)
        assertTrue(module.errors.size > 0)
        module.errors.forEach(::println)

        println(PrintPass(module).output)
    }

    @Test fun incorrectReassignmentType() {
        val code = """
        test () : Int
            let a = 7,
            a = "test",
            6
        """
        val module = compileString("incorrectReassignmentType", code)

        TypePass(module)
        assertTrue(module.errors.size > 0)
        module.errors.forEach(::println)

        println(PrintPass(module).output)
    }

    @Test fun correctTypeMarkingAndInference() {
        val code = """
        let d = 3 + 2
        let e = 0
        let f : Int
        let h = 6
        let i : Int = 7
        let j : Int = 8
        let str = "test"

        llvm (z, y, x, w : Int)
            var v = 42 + x,
            let u = 45 + v * 67 + 124 - (w * 4) / 5,
            v = v * 2 - z,
            5 + u * z * v

        foo (t, s : Int)
            let a = 7,
            let b = 9,
            let r : Int = 90128,
            if 1 != (2 + 2)
                var q = s,
                if 2 != 14 * 7 - 5
                    q = t
                else
                    if 4 >= 2
                        q = 7,
                        if s >= t || s <= 8
                            print(t, s)
                        ;
                    ;
                ;
            ;
            thing(a + b, a - b * g),
            a + b + 1

        class Object

            let field : Int = 0

            method (arg : Int)
                let local : Int = arg + 7,
                let thing : Int = local * 5,
                local / thing
        ;

        let variable_defined_after_class : Int = 0
        """
        val module = compileString("correctTypeMarkingAndInference", code)
        TypePass(module)

        assertEquals(T_INT32, module.globalClasses[0].methods[0].returnType)
        assertEquals(T_INT32, module.globalFunctions[0].returnType)
        assertEquals(T_INT32, ((module.globalFunctions[1].statements[3] as IfStatement).statements[0]
                as VariableDeclarationStatement).variable.type)

        println(PrintPass(module).output)
    }

    @Test fun incorrectConditionTypeInIfStatement() {
        val code = """
        test () : Int
            let a = 7,
            if a
                let b = 7
            ;
            6
        """
        val module = compileString("incorrectConditionTypeInIfStatement", code)

        TypePass(module)
        assertTrue(module.errors.size > 0)
        module.errors.forEach(::println)

        println(PrintPass(module).output)
    }

    @Test fun incorrectConditionTypeInWhileStatement() {
        val code = """
        test () : Int
            let a = 7,
            while a
                let b = 7
            ;
            6
        """
        val module = compileString("incorrectConditionTypeInWhileStatement", code)

        TypePass(module)
        assertTrue(module.errors.size > 0)
        module.errors.forEach(::println)

        println(PrintPass(module).output)
    }

    @Test fun inferVoidReturnType() {
        val code = """
        test1 ()
            let a = 7

        test2 (a : Int)
            if a < 10
                doWap()
            else
                damn()
            ;
        """
        val module = compileString("inferVoidReturnType", code)
        TypePass(module)

        assertEquals(T_VOID, module.globalFunctions[0].returnType)
        assertEquals(T_VOID, module.globalFunctions[1].returnType)

        println(PrintPass(module).output)
    }

    @Test fun incorrectTypeSigForVoidFunction() {
        val code = """
        test () : Int
            let a = 7,
            while a
                let b = 7
            ;
        """
        val module = compileString("incorrectTypeSigForVoidFunction", code)

        TypePass(module)
        assertTrue(module.errors.size > 0)
        module.errors.forEach(::println)

        println(PrintPass(module).output)
    }

    @Test fun inferReturnTypeForFunctions() {
        val code = """
        funA (a : Int) : Int
            a + 1

        funB (a : Int) : Int
            let b = funA(a),
            b * 2
        """
        val module = compileString("inferReturnTypeForFunctions", code)
        TypePass(module)

        assertEquals(T_INT32, (module.globalFunctions[1].statements[0] as VariableDeclarationStatement).variable.type)

        println(PrintPass(module).output)
    }

    @Test fun incorrectFunctionCallArguments() {
        val code = """
        funA (a : Int) : Int
            a + 1

        funB (a : Int) : Int
            let b = funA(true),
            b * 2
        """
        val module = compileString("incorrectFunctionCallArguments", code)

        TypePass(module)
        assertTrue(module.errors.size > 0)
        module.errors.forEach(::println)

        println(PrintPass(module).output)
    }

    @Test fun inferClassTypes() {
        val code = """
        class Test
            let a : Int = 7
            let b : Bool = true
        ;

        test (a : Test) : Test
            a
        """
        val module = compileString("inferClassTypes", code)
        TypePass(module)

        assertTrue(module.globalFunctions[0].returnType is Clazz, "${module.globalFunctions[0].returnType} is not a Class!")
        assertEquals("Test", (module.globalFunctions[0].returnType as Clazz).name)

        println(PrintPass(module).output)
    }

    @Test fun inferFieldTypes() {
        val code = """
        class Test
            let a : Int = 7
            let b : Bool = true
        ;

        test (thing : Test)
            thing.a
        """
        val module = compileString("inferFieldTypes", code)
        TypePass(module)

        assertEquals(T_INT32, module.globalFunctions[0].returnType, "${module.globalFunctions[0].returnType} is not an Int!")

        println(PrintPass(module).output)
    }

    @Test fun incorrectFieldSetterType() {
        val code = """
        class Test
            let a : Int = 7
            let b : Bool = true
        ;

        test (thing : Test)
            thing.a = thing
        """
        val module = compileString("incorrectFieldSetterType", code)

        TypePass(module)
        assertTrue(module.errors.size > 0)
        module.errors.forEach(::println)

        println(PrintPass(module).output)
    }

    @Test fun propagateFormalTypes() {
        val code = """
        add (a, b : Int)
            a + b
        """
        val module = compileString("propagateFormalTypes", code)
        TypePass(module)

        assertEquals(T_INT32, module.globalFunctions[0].returnType, "${module.globalFunctions[0].returnType} is not an Int!")

        println(PrintPass(module).output)
    }

    @Test fun inferFormalTypes() {
        val code = """
        add (a, b)
            a + b
        """
        val module = compileString("inferFormalTypes", code)
        TypePass(module)

        assertEquals(T_INT32, module.globalFunctions[0].formals[0].type, "${module.globalFunctions[0].formals[0].name} is not an Int!")
        assertEquals(T_INT32, module.globalFunctions[0].formals[1].type, "${module.globalFunctions[0].formals[1].name} is not an Int!")
        assertEquals(T_INT32, module.globalFunctions[0].returnType, "${module.globalFunctions[0].returnType} is not an Int!")

        println(PrintPass(module).output)
    }

    @Test fun floatTypes() {
        val code = """
        add (a, b)
            a +. b
        """
        val module = compileString("floatTypes", code)
        TypePass(module)

        assertEquals(T_FLOAT32, module.globalFunctions[0].formals[0].type, "${module.globalFunctions[0].formals[0].name} is not a Float32!")
        assertEquals(T_FLOAT32, module.globalFunctions[0].formals[1].type, "${module.globalFunctions[0].formals[1].name} is not a Float32!")
        assertEquals(T_FLOAT32, module.globalFunctions[0].returnType, "${module.globalFunctions[0].returnType} is not a Float32!")

        println(PrintPass(module).output)
    }

    @Test fun biggerFloatTypes() {
        val code = """
        add (a : Float64, b : Float128)
            a +. b
        """
        val module = compileString("biggerFloatTypes", code)
        TypePass(module)

        assertEquals(T_FLOAT64, module.globalFunctions[0].formals[0].type, "${module.globalFunctions[0].formals[0].name} is not a Float32!")
        assertEquals(T_FLOAT128, module.globalFunctions[0].formals[1].type, "${module.globalFunctions[0].formals[1].name} is not a Float32!")
        assertEquals(T_FLOAT128, module.globalFunctions[0].returnType, "${module.globalFunctions[0].returnType} is not a Float32!")

        println(PrintPass(module).output)
    }

    @Test fun inferFloatLiterals() {
        val code = """
        thing ()
            52.76d +. 215.0
        """
        val module = compileString("inferFloatLiterals", code)
        TypePass(module)

        assertEquals(T_FLOAT64, module.globalFunctions[0].returnType, "${module.globalFunctions[0].returnType} is not a Float32!")

        println(PrintPass(module).output)
    }

    @Test fun incorrectFloatLiteral() {
        val code = "let a : Float32 = 7.0d"
        val module = compileString("incorrectFloatLiteral", code)

        TypePass(module)
        assertTrue(module.errors.size > 0)
        module.errors.forEach(::println)

        println(PrintPass(module).output)
    }

}

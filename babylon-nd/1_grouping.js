// 1. Grouping Animations - dvi animaciju grupes
var createScene = function () {
    var scene = new BABYLON.Scene(engine);
    var camera = new BABYLON.ArcRotateCamera("cam", -Math.PI / 2, Math.PI / 3, 15, BABYLON.Vector3.Zero(), scene);
    camera.attachControl(canvas, true);
    new BABYLON.HemisphericLight("light", new BABYLON.Vector3(0, 1, 0), scene);

    // Objektai
    var mesh1 = BABYLON.MeshBuilder.CreateBox("mesh1", { size: 1.5 }, scene);
    mesh1.position.x = -4;
    var mesh2 = BABYLON.MeshBuilder.CreateSphere("mesh2", { diameter: 1 }, scene);
    var mesh3 = BABYLON.MeshBuilder.CreateSphere("mesh3", { diameter: 1 }, scene);
    var mesh4 = BABYLON.MeshBuilder.CreateSphere("mesh4", { diameter: 1 }, scene);
    mesh2.position.x = 1; mesh3.position.x = 3; mesh4.position.x = 5;

    var fps = 30;

    // animation1 - sukimasis aplink Y
    var animation1 = new BABYLON.Animation("rotY", "rotation.y", fps,
        BABYLON.Animation.ANIMATIONTYPE_FLOAT, BABYLON.Animation.ANIMATIONLOOPMODE_CYCLE);
    animation1.setKeys([{ frame: 0, value: 0 }, { frame: 60, value: 2 * Math.PI }]);

    // animation2 - dydzio keitimas
    var animation2 = new BABYLON.Animation("scale", "scaling", fps,
        BABYLON.Animation.ANIMATIONTYPE_VECTOR3, BABYLON.Animation.ANIMATIONLOOPMODE_CYCLE);
    animation2.setKeys([
        { frame: 0, value: new BABYLON.Vector3(1, 1, 1) },
        { frame: 30, value: new BABYLON.Vector3(1.5, 1.5, 1.5) },
        { frame: 60, value: new BABYLON.Vector3(1, 1, 1) }]);

    // animation3 - judejimas aukstyn/zemyn
    var animation3 = new BABYLON.Animation("posY", "position.y", fps,
        BABYLON.Animation.ANIMATIONTYPE_FLOAT, BABYLON.Animation.ANIMATIONLOOPMODE_CYCLE);
    animation3.setKeys([{ frame: 0, value: 0 }, { frame: 30, value: 2 }, { frame: 60, value: 0 }]);

    // animation4 - sokinejimas (naudojama 3 objektams)
    var animation4 = new BABYLON.Animation("jump", "position.y", fps,
        BABYLON.Animation.ANIMATIONTYPE_FLOAT, BABYLON.Animation.ANIMATIONLOOPMODE_CYCLE);
    animation4.setKeys([{ frame: 0, value: 0 }, { frame: 15, value: -2 }, { frame: 30, value: 0 }]);

    // GRUPE 1 - trys animacijos vienam objektui
    var animationGroup1 = new BABYLON.AnimationGroup("group1");
    animationGroup1.addTargetedAnimation(animation1, mesh1);
    animationGroup1.addTargetedAnimation(animation2, mesh1);
    animationGroup1.addTargetedAnimation(animation3, mesh1);

    // GRUPE 2 - viena animacija trims objektams
    var animationGroup2 = new BABYLON.AnimationGroup("group2");
    animationGroup2.addTargetedAnimation(animation4, mesh2);
    animationGroup2.addTargetedAnimation(animation4, mesh3);
    animationGroup2.addTargetedAnimation(animation4, mesh4);
    animationGroup2.speedRatio = 2; // 2x greiciau

    animationGroup1.play(true);
    animationGroup2.play(true);

    // Mygtukai (Babylon GUI)
    var ui = BABYLON.GUI.AdvancedDynamicTexture.CreateFullscreenUI("UI");
    var panel = new BABYLON.GUI.StackPanel();
    panel.horizontalAlignment = BABYLON.GUI.Control.HORIZONTAL_ALIGNMENT_LEFT;
    panel.verticalAlignment = BABYLON.GUI.Control.VERTICAL_ALIGNMENT_TOP;
    panel.width = "200px";
    ui.addControl(panel);

    var addButton = function (text, action) {
        var btn = BABYLON.GUI.Button.CreateSimpleButton(text, text);
        btn.width = "180px"; btn.height = "35px";
        btn.color = "white"; btn.background = "green";
        btn.paddingTop = "5px";
        btn.onPointerUpObservable.add(action);
        panel.addControl(btn);
    };
    addButton("Grupe 1 Play", function () { animationGroup1.play(true); });
    addButton("Grupe 1 Pause", function () { animationGroup1.pause(); });
    addButton("Grupe 1 Stop", function () { animationGroup1.stop(); });
    addButton("Grupe 2 Play", function () { animationGroup2.play(true); });
    addButton("Grupe 2 Pause", function () { animationGroup2.pause(); });
    addButton("Grupe 2 Stop", function () { animationGroup2.stop(); });

    return scene;
};

/*
  4. Mixamo veikejas (HVGirl.glb su Mixamo animacijomis: Idle, Walking, WalkingBack, Samba)
  Pagal Babylon.js dokumentacija "Animated Character".
  Valdymas: W - pirmyn, S - atgal, A/D - suktis, B - sokti (Samba)
*/
var createScene = function () {
    var scene = new BABYLON.Scene(engine);

    var camera = new BABYLON.ArcRotateCamera("cam", Math.PI / 2, Math.PI / 2.5, 10, new BABYLON.Vector3(0, 1, 0), scene);
    camera.attachControl(canvas, true);
    camera.lowerRadiusLimit = 2;
    camera.upperRadiusLimit = 15;
    new BABYLON.HemisphericLight("light", new BABYLON.Vector3(0, 1, 0), scene);
    BABYLON.MeshBuilder.CreateGround("ground", { width: 50, height: 50 }, scene);

    // Instrukcija ekrane
    var ui = BABYLON.GUI.AdvancedDynamicTexture.CreateFullscreenUI("UI");
    var help = new BABYLON.GUI.TextBlock();
    help.text = "W/S - eiti pirmyn/atgal, A/D - suktis, B - sokti";
    help.color = "white"; help.fontSize = 20;
    help.textVerticalAlignment = BABYLON.GUI.Control.VERTICAL_ALIGNMENT_TOP;
    help.paddingTop = "10px";
    ui.addControl(help);

    // Klaviaturos busena
    var inputMap = {};
    scene.actionManager = new BABYLON.ActionManager(scene);
    scene.actionManager.registerAction(new BABYLON.ExecuteCodeAction(BABYLON.ActionManager.OnKeyDownTrigger, function (evt) {
        inputMap[evt.sourceEvent.key.toLowerCase()] = evt.sourceEvent.type == "keydown";
    }));
    scene.actionManager.registerAction(new BABYLON.ExecuteCodeAction(BABYLON.ActionManager.OnKeyUpTrigger, function (evt) {
        inputMap[evt.sourceEvent.key.toLowerCase()] = evt.sourceEvent.type == "keydown";
    }));

    // Modelio ikelimas
    BABYLON.SceneLoader.ImportMesh("", "https://assets.babylonjs.com/meshes/", "HVGirl.glb", scene, function (newMeshes) {
        var hero = newMeshes[0];
        hero.scaling.scaleInPlace(0.1);   // sumaziname modeli
        camera.target = hero;             // kamera seka veikeja

        var heroSpeed = 0.03;
        var heroSpeedBackwards = 0.01;
        var heroRotationSpeed = 0.1;
        var animating = true;

        // Animaciju grupes is modelio
        var walkAnim = scene.getAnimationGroupByName("Walking");
        var walkBackAnim = scene.getAnimationGroupByName("WalkingBack");
        var idleAnim = scene.getAnimationGroupByName("Idle");
        var sambaAnim = scene.getAnimationGroupByName("Samba");

        // Kiekviename kadre tikriname paspaustus mygtukus
        scene.onBeforeRenderObservable.add(function () {
            var keydown = false;
            if (inputMap["w"]) { hero.moveWithCollisions(hero.forward.scaleInPlace(heroSpeed)); keydown = true; }
            if (inputMap["s"]) { hero.moveWithCollisions(hero.forward.scaleInPlace(-heroSpeedBackwards)); keydown = true; }
            if (inputMap["a"]) { hero.rotate(BABYLON.Vector3.Up(), -heroRotationSpeed); keydown = true; }
            if (inputMap["d"]) { hero.rotate(BABYLON.Vector3.Up(), heroRotationSpeed); keydown = true; }
            if (inputMap["b"]) { keydown = true; }

            if (keydown) {
                if (!animating) {
                    animating = true;
                    if (inputMap["s"]) {
                        walkBackAnim.start(true, 1.0, walkBackAnim.from, walkBackAnim.to, false);
                    } else if (inputMap["b"]) {
                        sambaAnim.start(true, 1.0, sambaAnim.from, sambaAnim.to, false);
                    } else {
                        walkAnim.start(true, 1.0, walkAnim.from, walkAnim.to, false);
                    }
                }
            } else if (animating) {
                // nieko nespaudziama - stovi (Idle)
                idleAnim.start(true, 1.0, idleAnim.from, idleAnim.to, false);
                sambaAnim.stop();
                walkAnim.stop();
                walkBackAnim.stop();
                animating = false;
            }
        });
    });

    return scene;
};

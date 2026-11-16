/*
  5. Animuotas mesh is Babylon bibliotekos: Xbot.glb
  (https://assets.babylonjs.com/meshes/Xbot.glb)

  Kodo paaiskinimas:
  1. Sukuriama scena, kamera, sviesa ir zeme.
  2. BABYLON.SceneLoader.ImportMeshAsync() ikelia .glb faila is interneto.
  3. result.meshes - ikelti mesh'ai (meshes[0] - pagrindinis __root__),
     result.animationGroups - modelyje esancios animacijos
     (idle, walk, run, sad_pose, sneak_pose, headShake, agree).
  4. Visos animacijos sustabdomos ir paleidziama "idle".
  5. Kiekvienai animacijai sukuriamas GUI mygtukas: paspaudus sustabdoma
     esama animacija ir paleidziama pasirinkta (start(true) - kartoti be galo).
  6. Slankiklis keicia animacijos greiti (speedRatio).
  7. Mygtukas "Sukti" ijungia/isjungia modelio sukima aplink Y asi.
*/
var createScene = function () {
    var scene = new BABYLON.Scene(engine);
    var camera = new BABYLON.ArcRotateCamera("cam", Math.PI / 2, Math.PI / 2.5, 4, new BABYLON.Vector3(0, 1, 0), scene);
    camera.attachControl(canvas, true);
    new BABYLON.HemisphericLight("light", new BABYLON.Vector3(0, 1, 0), scene);
    BABYLON.MeshBuilder.CreateGround("ground", { width: 10, height: 10 }, scene);

    var ui = BABYLON.GUI.AdvancedDynamicTexture.CreateFullscreenUI("UI");
    var panel = new BABYLON.GUI.StackPanel();
    panel.width = "200px";
    panel.horizontalAlignment = BABYLON.GUI.Control.HORIZONTAL_ALIGNMENT_LEFT;
    panel.verticalAlignment = BABYLON.GUI.Control.VERTICAL_ALIGNMENT_TOP;
    ui.addControl(panel);

    BABYLON.SceneLoader.ImportMeshAsync("", "https://assets.babylonjs.com/meshes/", "Xbot.glb", scene).then(function (result) {
        var root = result.meshes[0];
        var groups = result.animationGroups;
        var speed = 1;
        var rotating = false;

        groups.forEach(function (g) { g.stop(); });
        var current = scene.getAnimationGroupByName("idle");
        current.start(true);

        // Mygtukas kiekvienai animacijai
        groups.forEach(function (g) {
            var btn = BABYLON.GUI.Button.CreateSimpleButton(g.name, g.name);
            btn.width = "180px"; btn.height = "35px";
            btn.color = "white"; btn.background = "green";
            btn.paddingTop = "5px";
            btn.onPointerUpObservable.add(function () {
                current.stop();
                current = g;
                current.start(true, speed);
            });
            panel.addControl(btn);
        });

        // Greicio slankiklis
        var label = new BABYLON.GUI.TextBlock();
        label.text = "Greitis: 1"; label.height = "30px"; label.color = "white";
        panel.addControl(label);
        var slider = new BABYLON.GUI.Slider();
        slider.minimum = 0.1; slider.maximum = 3; slider.value = 1;
        slider.height = "20px"; slider.width = "180px";
        slider.onValueChangedObservable.add(function (v) {
            speed = v;
            current.speedRatio = v;
            label.text = "Greitis: " + v.toFixed(1);
        });
        panel.addControl(slider);

        // Sukimas
        var rotBtn = BABYLON.GUI.Button.CreateSimpleButton("rot", "Sukti");
        rotBtn.width = "180px"; rotBtn.height = "40px";
        rotBtn.color = "white"; rotBtn.background = "blue";
        rotBtn.paddingTop = "5px";
        rotBtn.onPointerUpObservable.add(function () { rotating = !rotating; });
        panel.addControl(rotBtn);

        scene.onBeforeRenderObservable.add(function () {
            if (rotating) root.rotate(BABYLON.Vector3.Up(), 0.01);
        });
    });

    return scene;
};

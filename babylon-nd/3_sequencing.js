/*
  3. Sequencing Animations - 5 performeriai

  SEQUENCE TIMETABLE (frameRate = 20, 1 s = 20 kadru)
  ---------------------------------------------------------
  Performer   | Veiksmas                 | Pradzia | Pabaiga
  ---------------------------------------------------------
  1. box      | pavaziuoja i desine      |  0 s    |  2 s
  2. sphere   | pasoka aukstyn           |  2 s    |  3 s
  3. cylinder | apsiverčia               |  3 s    |  5 s
  4. torus    | padideja ir sumazeja     |  5 s    |  6 s
  5. cone     | nukrenta zemyn           |  6 s    |  8 s
  ---------------------------------------------------------
  Visi paleidziami kartu, o eiliskuma nustato raktu kadrai:
  iki savo pradzios performeris stovi vietoje.
*/
var createScene = function () {
    var scene = new BABYLON.Scene(engine);
    var camera = new BABYLON.ArcRotateCamera("cam", -Math.PI / 2, Math.PI / 3, 18, BABYLON.Vector3.Zero(), scene);
    camera.attachControl(canvas, true);
    new BABYLON.HemisphericLight("light", new BABYLON.Vector3(0, 1, 0), scene);
    BABYLON.MeshBuilder.CreateGround("ground", { width: 20, height: 10 }, scene).position.y = -0.5;

    var frameRate = 20;
    var s = frameRate;   // 1 sekunde
    var end = 8 * s;     // sekos pabaiga

    // 5 performeriai
    var box = BABYLON.MeshBuilder.CreateBox("box", {}, scene);
    box.position = new BABYLON.Vector3(-6, 0, 0);
    var sphere = BABYLON.MeshBuilder.CreateSphere("sphere", {}, scene);
    sphere.position = new BABYLON.Vector3(-3, 0, 0);
    var cylinder = BABYLON.MeshBuilder.CreateCylinder("cylinder", { height: 1.5 }, scene);
    var torus = BABYLON.MeshBuilder.CreateTorus("torus", { diameter: 1.2, thickness: 0.3 }, scene);
    torus.position = new BABYLON.Vector3(3, 0, 0);
    var cone = BABYLON.MeshBuilder.CreateCylinder("cone", { diameterTop: 0, height: 1.5 }, scene);
    cone.position = new BABYLON.Vector3(6, 4, 0);

    var F = BABYLON.Animation.ANIMATIONTYPE_FLOAT;
    var V = BABYLON.Animation.ANIMATIONTYPE_VECTOR3;
    var makeAnim = function (name, prop, type, keys) {
        var a = new BABYLON.Animation(name, prop, frameRate, type, BABYLON.Animation.ANIMATIONLOOPMODE_CONSTANT);
        a.setKeys(keys);
        return a;
    };

    // 1. box: 0-2 s
    var boxAnim = makeAnim("boxMove", "position.x", F, [
        { frame: 0, value: -6 }, { frame: 2 * s, value: -4 }, { frame: end, value: -4 }]);
    // 2. sphere: 2-3 s
    var sphereAnim = makeAnim("sphereJump", "position.y", F, [
        { frame: 0, value: 0 }, { frame: 2 * s, value: 0 }, { frame: 2.5 * s, value: 3 },
        { frame: 3 * s, value: 0 }, { frame: end, value: 0 }]);
    // 3. cylinder: 3-5 s
    var cylAnim = makeAnim("cylRotate", "rotation.z", F, [
        { frame: 0, value: 0 }, { frame: 3 * s, value: 0 }, { frame: 5 * s, value: 2 * Math.PI },
        { frame: end, value: 2 * Math.PI }]);
    // 4. torus: 5-6 s
    var torusAnim = makeAnim("torusScale", "scaling", V, [
        { frame: 0, value: new BABYLON.Vector3(1, 1, 1) }, { frame: 5 * s, value: new BABYLON.Vector3(1, 1, 1) },
        { frame: 5.5 * s, value: new BABYLON.Vector3(2, 2, 2) }, { frame: 6 * s, value: new BABYLON.Vector3(1, 1, 1) },
        { frame: end, value: new BABYLON.Vector3(1, 1, 1) }]);
    // 5. cone: 6-8 s
    var coneAnim = makeAnim("coneFall", "position.y", F, [
        { frame: 0, value: 4 }, { frame: 6 * s, value: 4 }, { frame: 8 * s, value: 0.25 }]);

    var start = function () {
        scene.stopAllAnimations();
        scene.beginDirectAnimation(box, [boxAnim], 0, end, false);
        scene.beginDirectAnimation(sphere, [sphereAnim], 0, end, false);
        scene.beginDirectAnimation(cylinder, [cylAnim], 0, end, false);
        scene.beginDirectAnimation(torus, [torusAnim], 0, end, false);
        scene.beginDirectAnimation(cone, [coneAnim], 0, end, false);
    };
    start();

    // Mygtukas paleisti is naujo
    var ui = BABYLON.GUI.AdvancedDynamicTexture.CreateFullscreenUI("UI");
    var btn = BABYLON.GUI.Button.CreateSimpleButton("restart", "Paleisti is naujo");
    btn.width = "180px"; btn.height = "40px";
    btn.color = "white"; btn.background = "green";
    btn.verticalAlignment = BABYLON.GUI.Control.VERTICAL_ALIGNMENT_TOP;
    btn.onPointerUpObservable.add(start);
    ui.addControl(btn);

    return scene;
};

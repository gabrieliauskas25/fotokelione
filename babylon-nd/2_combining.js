// 2. Combining Animations - kelios animacijos sujungtos vienam objektui
var createScene = function () {
    var scene = new BABYLON.Scene(engine);
    var camera = new BABYLON.ArcRotateCamera("cam", -Math.PI / 2, Math.PI / 3, 12, BABYLON.Vector3.Zero(), scene);
    camera.attachControl(canvas, true);
    new BABYLON.HemisphericLight("light", new BABYLON.Vector3(0, 1, 0), scene);

    var box = BABYLON.MeshBuilder.CreateBox("box", { size: 1 }, scene);
    box.material = new BABYLON.StandardMaterial("mat", scene);
    var frameRate = 10;

    // anim1 - judejimas X asimi
    var anim1 = new BABYLON.Animation("xSlide", "position.x", frameRate,
        BABYLON.Animation.ANIMATIONTYPE_FLOAT, BABYLON.Animation.ANIMATIONLOOPMODE_CYCLE);
    anim1.setKeys([
        { frame: 0, value: 2 },
        { frame: frameRate, value: -2 },
        { frame: 2 * frameRate, value: 2 }]);

    // anim2 - sukimasis aplink X
    var anim2 = new BABYLON.Animation("xRot", "rotation.x", frameRate,
        BABYLON.Animation.ANIMATIONTYPE_FLOAT, BABYLON.Animation.ANIMATIONLOOPMODE_CYCLE);
    anim2.setKeys([
        { frame: 0, value: 0 },
        { frame: frameRate, value: Math.PI },
        { frame: 2 * frameRate, value: 2 * Math.PI }]);

    // anim3 - spalvos keitimas
    var anim3 = new BABYLON.Animation("color", "material.diffuseColor", frameRate,
        BABYLON.Animation.ANIMATIONTYPE_COLOR3, BABYLON.Animation.ANIMATIONLOOPMODE_CYCLE);
    anim3.setKeys([
        { frame: 0, value: new BABYLON.Color3(1, 0, 0) },
        { frame: frameRate, value: new BABYLON.Color3(0, 0, 1) },
        { frame: 2 * frameRate, value: new BABYLON.Color3(1, 0, 0) }]);

    // SUJUNGIMAS - visos animacijos paleidziamos kartu
    scene.beginDirectAnimation(box, [anim1, anim2, anim3], 0, 2 * frameRate, true);

    return scene;
};

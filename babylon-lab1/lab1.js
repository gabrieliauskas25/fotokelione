// =====================================================================
// 1 laboratorinis darbas – Babylon.js
// Paleidimas: nukopijuoti visą failą į https://playground.babylonjs.com
//
// Kriterijai ir kur jie įgyvendinti:
//  1) Pivot + instance ............ "PIVOT IR INSTANCE" (kubų žiedas, vartai)
//  2) Meshas iš asset bibliotekos . "MESHAS IŠ BIBLIOTEKOS" (HVGirl.glb)
//  3) Šešėliai + veidrodis ........ "ŠEŠĖLIAI" ir "VEIDRODIS"
//  4) Mesh interactions ........... "SUSIKIRTIMAI" (intersectsMesh / intersectsPoint)
//  5) Dalelių sistema (2 vnt.) .... "DALELĖS" (fontanas + sprogimas)
//  6) Įvykių tvarkyklė ............ "ĮVYKIAI" (klaviatūra, pelė, ActionManager, GUI)
//  7) Savas pavyzdys .............. "SAVAS PAVYZDYS": Volumetric Light Scattering
//     (saulės spinduliai, post-process) + animacija su easing funkcija,
//     kurioje saulė juda dangumi, o kryptinė šviesa ir šešėliai seka paskui ją.
// =====================================================================
export const createScene = function () {
    var scene = new BABYLON.Scene(engine);
    scene.clearColor = new BABYLON.Color4(0.02, 0.02, 0.08, 1);

    // Kamera
    var camera = new BABYLON.ArcRotateCamera("kamera", -Math.PI / 2, 1.25, 30, new BABYLON.Vector3(0, 2, 0), scene);
    camera.attachControl(canvas, true);
    camera.lowerRadiusLimit = 8;
    camera.upperRadiusLimit = 60;
    camera.upperBetaLimit = 1.45; // neleidžiam kamerai nusileisti po žeme

    // Šviesos
    var hemi = new BABYLON.HemisphericLight("hemi", new BABYLON.Vector3(0, 1, 0), scene);
    hemi.intensity = 0.35;
    var sviesa = new BABYLON.DirectionalLight("sviesa", new BABYLON.Vector3(-1, -2, -1), scene);
    sviesa.position = new BABYLON.Vector3(20, 40, 20);
    sviesa.intensity = 0.9;

    // ---------------- ŠEŠĖLIAI ----------------
    var seseliai = new BABYLON.ShadowGenerator(2048, sviesa);
    seseliai.useBlurExponentialShadowMap = true;
    seseliai.blurKernel = 32;

    // ---------------- VEIDRODIS ----------------
    var zeme = BABYLON.MeshBuilder.CreateGround("zeme", { width: 40, height: 40 }, scene);
    var zm = new BABYLON.StandardMaterial("zm", scene);
    zm.diffuseColor = new BABYLON.Color3(0.15, 0.15, 0.2);
    zm.specularColor = BABYLON.Color3.Black();
    var mt = new BABYLON.MirrorTexture("mt", 1024, scene, true);
    mt.mirrorPlane = new BABYLON.Plane(0, -1, 0, 0); // žemė yra y = 0
    mt.level = 0.5;
    mt.renderParticles = true;                         // atspindi ir daleles
    zm.reflectionTexture = mt;
    zeme.material = zm;
    zeme.receiveShadows = true;

    // pagalbinė funkcija: objektas meta šešėlį ir atsispindi veidrodyje
    function iSceną(m) {
        seseliai.addShadowCaster(m);
        mt.renderList.push(m);
    }

    // ---------------- PIVOT IR INSTANCE ----------------
    // pivotas – tuščias TransformNode žemės centre; sukant jį sukasi visi kubai
    var pivotas = new BABYLON.TransformNode("pivotas", scene);
    pivotas.position.y = 1;

    var kubas = BABYLON.MeshBuilder.CreateBox("kubas", { size: 1 }, scene);
    var km = new BABYLON.StandardMaterial("km", scene);
    km.diffuseColor = new BABYLON.Color3(0.2, 0.6, 1);
    kubas.material = km;
    kubas.parent = pivotas;
    kubas.position.x = 9;
    iSceną(kubas);

    var kubai = [kubas];
    for (var i = 1; i < 8; i++) {
        var inst = kubas.createInstance("kubas" + i); // instance – ta pati geometrija ir medžiaga
        inst.parent = pivotas;
        var a = i * Math.PI * 2 / 8;
        inst.position = new BABYLON.Vector3(9 * Math.cos(a), 0, 9 * Math.sin(a));
        iSceną(inst);
        kubai.push(inst);
    }

    // Vartai – sukasi ne apie savo centrą, o apie kraštą (setPivotPoint)
    var vartai = BABYLON.MeshBuilder.CreateBox("vartai", { width: 4, height: 3, depth: 0.3 }, scene);
    vartai.position = new BABYLON.Vector3(-12, 1.5, -6);
    vartai.setPivotPoint(new BABYLON.Vector3(-2, 0, 0)); // vyris kairiajame krašte
    var vm = new BABYLON.StandardMaterial("vm", scene);
    vm.diffuseColor = new BABYLON.Color3(0.6, 0.35, 0.15);
    vartai.material = vm;
    iSceną(vartai);

    // ---------------- DALELĖS ----------------
    // 1) Fontanas centre (nuolatinė sistema su gravitacija)
    var fontanas = BABYLON.MeshBuilder.CreateCylinder("fontanas", { diameterTop: 1, diameterBottom: 2, height: 1 }, scene);
    fontanas.position.y = 0.5;
    iSceną(fontanas);

    var ps = new BABYLON.ParticleSystem("fontanoDaleles", 3000, scene);
    ps.particleTexture = new BABYLON.Texture("textures/flare.png", scene);
    ps.emitter = new BABYLON.Vector3(0, 1, 0);
    ps.minEmitBox = new BABYLON.Vector3(-0.2, 0, -0.2);
    ps.maxEmitBox = new BABYLON.Vector3(0.2, 0, 0.2);
    ps.color1 = new BABYLON.Color4(0.3, 0.7, 1, 1);
    ps.color2 = new BABYLON.Color4(0.6, 0.2, 1, 1);
    ps.colorDead = new BABYLON.Color4(0, 0, 0.2, 0);
    ps.minSize = 0.1; ps.maxSize = 0.4;
    ps.minLifeTime = 1; ps.maxLifeTime = 2;
    ps.emitRate = 800;
    ps.blendMode = BABYLON.ParticleSystem.BLENDMODE_ONEONE;
    ps.gravity = new BABYLON.Vector3(0, -9.81, 0);
    ps.direction1 = new BABYLON.Vector3(-1.5, 8, -1.5);
    ps.direction2 = new BABYLON.Vector3(1.5, 10, 1.5);
    ps.minEmitPower = 1; ps.maxEmitPower = 1.5;
    ps.updateSpeed = 0.01;
    ps.start();

    // 2) Sprogimas – nieko neišskiria, kol nepaprašom (manualEmitCount)
    var sprogimas = new BABYLON.ParticleSystem("sprogimas", 1000, scene);
    sprogimas.particleTexture = new BABYLON.Texture("textures/flare.png", scene);
    sprogimas.emitter = new BABYLON.Vector3(0, 0, 0);
    sprogimas.createSphereEmitter(0.5);
    sprogimas.color1 = new BABYLON.Color4(1, 0.8, 0.2, 1);
    sprogimas.color2 = new BABYLON.Color4(1, 0.2, 0.1, 1);
    sprogimas.colorDead = new BABYLON.Color4(0, 0, 0, 0);
    sprogimas.minSize = 0.15; sprogimas.maxSize = 0.5;
    sprogimas.minLifeTime = 0.4; sprogimas.maxLifeTime = 1;
    sprogimas.minEmitPower = 4; sprogimas.maxEmitPower = 8;
    sprogimas.emitRate = 0;
    sprogimas.blendMode = BABYLON.ParticleSystem.BLENDMODE_ONEONE;
    sprogimas.start();

    function sprogti(vieta) {
        sprogimas.emitter = vieta.clone();
        sprogimas.manualEmitCount = 300;
    }

    // ---------------- SAVAS PAVYZDYS ----------------
    // Saulė + Volumetric Light Scattering (spinduliai pro objektus)
    var saule = BABYLON.MeshBuilder.CreateSphere("saule", { diameter: 3 }, scene);
    var sm = new BABYLON.StandardMaterial("sm", scene);
    sm.emissiveColor = new BABYLON.Color3(1, 0.85, 0.4);
    sm.diffuseColor = BABYLON.Color3.Black();
    sm.disableLighting = true;
    saule.material = sm;
    mt.renderList.push(saule);

    var vls = new BABYLON.VolumetricLightScatteringPostProcess(
        "vls", 1.0, camera, saule, 100, BABYLON.Texture.BILINEAR_SAMPLINGMODE, engine, false);
    vls.exposure = 0.25;
    vls.decay = 0.97;
    vls.weight = 0.6;
    vls.density = 0.93;

    // Saulės judėjimo animacija su easing funkcija
    var sauleAnim = new BABYLON.Animation("sauleAnim", "position", 30,
        BABYLON.Animation.ANIMATIONTYPE_VECTOR3, BABYLON.Animation.ANIMATIONLOOPMODE_CYCLE);
    sauleAnim.setKeys([
        { frame: 0,   value: new BABYLON.Vector3(-22, 5, 25) },
        { frame: 150, value: new BABYLON.Vector3(0, 11, 25) },
        { frame: 300, value: new BABYLON.Vector3(22, 5, 25) },
        { frame: 450, value: new BABYLON.Vector3(0, 11, 25) },
        { frame: 600, value: new BABYLON.Vector3(-22, 5, 25) }
    ]);
    var ease = new BABYLON.SineEase();
    ease.setEasingMode(BABYLON.EasingFunction.EASINGMODE_EASEINOUT);
    sauleAnim.setEasingFunction(ease);
    saule.animations.push(sauleAnim);
    scene.beginAnimation(saule, 0, 600, true);

    // ---------------- MESHAS IŠ BIBLIOTEKOS ----------------
    var mergina = null, kolideris = null;
    var anim = {};
    BABYLON.SceneLoader.ImportMeshAsync("", "https://assets.babylonjs.com/meshes/", "HVGirl.glb", scene)
        .then(function (rez) {
            mergina = rez.meshes[0];
            mergina.scaling.scaleInPlace(0.1);
            mergina.position = new BABYLON.Vector3(0, 0, -8);
            rez.meshes.forEach(iSceną);

            anim.idle = scene.getAnimationGroupByName("Idle");
            anim.walk = scene.getAnimationGroupByName("Walking");
            anim.back = scene.getAnimationGroupByName("WalkingBack");
            anim.samba = scene.getAnimationGroupByName("Samba");
            rez.animationGroups.forEach(function (g) { g.stop(); });
            anim.idle.start(true);

            // nematomas "kūnas" susikirtimams tikrinti
            kolideris = BABYLON.MeshBuilder.CreateBox("kolideris", { width: 1, height: 3, depth: 1 }, scene);
            kolideris.isVisible = false;
            kolideris.isPickable = false;
        });

    // ---------------- GUI ----------------
    var ui = BABYLON.GUI.AdvancedDynamicTexture.CreateFullscreenUI("UI");
    var sukasi = true;
    var btn = BABYLON.GUI.Button.CreateSimpleButton("btn", "Stabdyti žiedą");
    btn.width = "160px"; btn.height = "40px";
    btn.color = "white"; btn.background = "#335";
    btn.top = "10px";
    btn.verticalAlignment = BABYLON.GUI.Control.VERTICAL_ALIGNMENT_TOP;
    ui.addControl(btn);
    btn.onPointerUpObservable.add(function () {
        sukasi = !sukasi;
        btn.textBlock.text = sukasi ? "Stabdyti žiedą" : "Sukti žiedą";
    });

    var surinkta = 0;
    var info = new BABYLON.GUI.TextBlock("info",
        "W/S – eiti, A/D – suktis, Space – šokis\nPaspausk kubą arba vartus\nSurinkta: 0");
    info.color = "white"; info.fontSize = 16;
    info.textHorizontalAlignment = BABYLON.GUI.Control.HORIZONTAL_ALIGNMENT_LEFT;
    info.textVerticalAlignment = BABYLON.GUI.Control.VERTICAL_ALIGNMENT_BOTTOM;
    info.paddingLeft = "12px"; info.paddingBottom = "12px";
    ui.addControl(info);
    function atnaujintiInfo() {
        info.text = "W/S – eiti, A/D – suktis, Space – šokis\nPaspausk kubą arba vartus\nSurinkta: " + surinkta;
    }

    // ---------------- ĮVYKIAI ----------------
    // 1) Klaviatūra – laikom, kurie klavišai nuspausti
    var klavisai = {};
    scene.onKeyboardObservable.add(function (kb) {
        var k = kb.event.key.toLowerCase();
        if (kb.type === BABYLON.KeyboardEventTypes.KEYDOWN) {
            klavisai[k] = true;
            if (k === " " && anim.samba) sprogti(mergina.position.add(new BABYLON.Vector3(0, 3, 0)));
        } else if (kb.type === BABYLON.KeyboardEventTypes.KEYUP) {
            klavisai[k] = false;
        }
    });

    // 2) Pelė – paspaudus kubą jis "sprogsta"
    scene.onPointerObservable.add(function (pi) {
        if (pi.type !== BABYLON.PointerEventTypes.POINTERPICK) return;
        var m = pi.pickInfo.pickedMesh;
        if (m && m.name.indexOf("kubas") === 0) sprogti(pi.pickInfo.pickedPoint);
    });

    // 3) ActionManager – vartai: užvedus pelę šviečia, paspaudus atsidaro/užsidaro
    var atidaryti = false;
    vartai.actionManager = new BABYLON.ActionManager(scene);
    vartai.actionManager.registerAction(new BABYLON.SetValueAction(
        BABYLON.ActionManager.OnPointerOverTrigger, vm, "emissiveColor", new BABYLON.Color3(0.3, 0.2, 0)));
    vartai.actionManager.registerAction(new BABYLON.SetValueAction(
        BABYLON.ActionManager.OnPointerOutTrigger, vm, "emissiveColor", BABYLON.Color3.Black()));
    vartai.actionManager.registerAction(new BABYLON.ExecuteCodeAction(
        BABYLON.ActionManager.OnPickTrigger, function () {
            atidaryti = !atidaryti;
            var bounce = new BABYLON.BounceEase(2, 3);
            bounce.setEasingMode(BABYLON.EasingFunction.EASINGMODE_EASEOUT);
            BABYLON.Animation.CreateAndStartAnimation("vartuAnim", vartai, "rotation.y", 30, 30,
                vartai.rotation.y, atidaryti ? -Math.PI / 2 : 0,
                BABYLON.Animation.ANIMATIONLOOPMODE_CONSTANT, bounce);
        }));

    // 4) Kiekvieno kadro logika
    var dabartine = null;
    function groti(g) {
        if (!g || dabartine === g) return;
        if (dabartine) dabartine.stop();
        g.start(true);
        dabartine = g;
    }

    scene.onBeforeRenderObservable.add(function () {
        var dt = engine.getDeltaTime() / 1000;

        // pivoto sukimas – visi instance kubai skrieja aplink centrą
        if (sukasi) pivotas.rotation.y += 0.4 * dt;
        kubai.forEach(function (k) { k.rotation.y += 1.5 * dt; });

        // šviesa ir šešėliai seka saulę
        sviesa.direction = saule.position.negate().normalize();
        sviesa.position = saule.position.clone();

        if (!mergina) return;

        // merginos valdymas
        var greitis = 4 * dt;
        if (klavisai["a"]) mergina.rotate(BABYLON.Vector3.Up(), -2 * dt);
        if (klavisai["d"]) mergina.rotate(BABYLON.Vector3.Up(), 2 * dt);
        if (klavisai["w"]) {
            mergina.position.addInPlace(mergina.forward.scale(greitis));
            groti(anim.walk);
        } else if (klavisai["s"]) {
            mergina.position.addInPlace(mergina.forward.scale(-greitis * 0.5));
            groti(anim.back);
        } else if (klavisai[" "]) {
            groti(anim.samba);
        } else if (klavisai["a"] || klavisai["d"]) {
            groti(anim.walk);
        } else {
            groti(anim.idle);
        }

        // ---------------- SUSIKIRTIMAI ----------------
        kolideris.position = mergina.position.add(new BABYLON.Vector3(0, 1.5, 0));
        kolideris.computeWorldMatrix(true);

        // intersectsMesh – mergina paliečia kubą: jis išsipučia ir sprogsta
        kubai.forEach(function (k) {
            if (kolideris.intersectsMesh(k, false)) {
                if (!k.palietas) {
                    k.palietas = true;
                    surinkta++;
                    atnaujintiInfo();
                    sprogti(k.getAbsolutePosition());
                }
                k.scaling.setAll(1.6);
            } else {
                k.palietas = false;
                k.scaling = BABYLON.Vector3.Lerp(k.scaling, BABYLON.Vector3.One(), 0.1);
            }
        });

        // intersectsPoint – mergina įlipa į fontaną: jis trykšta stipriau
        var prieFontano = fontanas.intersectsPoint(mergina.position.add(new BABYLON.Vector3(0, 0.5, 0)));
        ps.emitRate = prieFontano ? 2500 : 800;
        ps.maxEmitPower = prieFontano ? 2.5 : 1.5;
    });

    return scene;
};

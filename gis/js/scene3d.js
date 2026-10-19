require({
  packages: [{ name: "app", location: location.pathname.replace(/\/[^/]*$/, "") + "/js" }]
}, [
  "esri/Map",
  "esri/views/SceneView",
  "esri/Graphic",
  "esri/layers/GraphicsLayer",
  "esri/layers/SceneLayer",
  "esri/core/reactiveUtils",
  "esri/widgets/Home",
  "esri/widgets/BasemapToggle",
  "esri/widgets/LayerList",
  "esri/widgets/Legend",
  "esri/widgets/Daylight",
  "esri/widgets/Weather",
  "esri/widgets/Expand",
  "esri/widgets/Fullscreen",
  "app/common"
], function (
  Map, SceneView, Graphic, GraphicsLayer, SceneLayer, reactiveUtils,
  Home, BasemapToggle, LayerList, Legend, Daylight, Weather, Expand, Fullscreen,
  common
) {
  const $ = (id) => document.getElementById(id);

  // ---------------- 2. Sluoksniai su 3D simboliais ----------------
  function cilindras(spalva) {
    return {
      type: "point-3d",
      symbolLayers: [{
        type: "object",
        resource: { primitive: "cylinder" },
        width: 3500,
        material: { color: spalva }
      }]
    };
  }

  const miestai = common.sukurtiMiestus({
    elevationInfo: { mode: "on-the-ground" },
    maxScale: 150000,           // priartinus stulpeliai paslepiami, kad neužstotų vaizdo
    renderer: {
      type: "unique-value",
      field: "tipas",
      legendOptions: { title: "Miesto tipas" },
      uniqueValueInfos: Object.keys(common.TIPU_SPALVOS).map(function (t) {
        return { value: t, label: t, symbol: cilindras(common.TIPU_SPALVOS[t]) };
      }),
      visualVariables: [
        {
          type: "size",
          field: "gyventojai",
          axis: "height",
          legendOptions: { title: "Gyventojai (stulpelio aukštis)" },
          stops: [
            { value: 2000, size: 1500 },
            { value: 600000, size: 40000 }
          ]
        },
        { type: "size", axis: "width-and-depth", useSymbolValue: true }
      ]
    },
    labelingInfo: [{
      labelExpressionInfo: { expression: "$feature.pavadinimas" },
      where: "gyventojai >= 20000 OR tipas = 'Kurortas'",
      symbol: {
        type: "label-3d",
        symbolLayers: [{
          type: "text",
          material: { color: "#1d1d1d" },
          halo: { color: "white", size: 1.5 },
          size: 11,
          font: { weight: "bold" }
        }]
      }
    }]
  });

  const vietos = common.sukurtiVietas({
    elevationInfo: { mode: "relative-to-ground" },
    featureReduction: { type: "selection" },
    renderer: {
      type: "simple",
      symbol: {
        type: "point-3d",
        symbolLayers: [{
          type: "icon",
          resource: { primitive: "circle" },
          size: 12,
          material: { color: [193, 39, 45] },
          outline: { color: "white", size: 2 }
        }],
        verticalOffset: { screenLength: 35, maxWorldLength: 2000, minWorldLength: 20 },
        callout: { type: "line", size: 1.2, color: "white", border: { color: [60, 60, 60] } }
      }
    },
    labelingInfo: [{
      labelExpressionInfo: { expression: "$feature.pavadinimas" },
      symbol: {
        type: "label-3d",
        symbolLayers: [{ type: "text", material: { color: "white" }, halo: { color: [60, 60, 60], size: 1 }, size: 10 }]
      }
    }]
  });

  const apskritys = common.sukurtiApskritis();

  const pastatai = new SceneLayer({
    title: "OpenStreetMap 3D pastatai",
    portalItem: { id: "ca0470dbbddb4db28bad74ed39949e25" },
    popupEnabled: false
  });

  const grafika = new GraphicsLayer({
    title: "Gedimino bokšto modelis (3D grafika)",
    elevationInfo: { mode: "relative-to-ground" }
  });

  // ---------------- 1. Scenos įkėlimas (3D) ----------------
  const map = new Map({
    basemap: "satellite",
    ground: "world-elevation",
    layers: [apskritys, pastatai, miestai, vietos, grafika]
  });

  const view = new SceneView({
    container: "viewDiv",
    map: map,
    qualityProfile: "high",
    camera: {
      position: { longitude: 23.95, latitude: 52.95, z: 260000 },
      heading: 0,
      tilt: 48
    },
    environment: {
      lighting: { type: "sun", directShadowsEnabled: true },
      atmosphereEnabled: true,
      starsEnabled: true
    },
    popup: { dockEnabled: false, dockOptions: { breakpoint: false } }
  });

  // ---------------- 3. Valdikliai ----------------
  view.ui.add(new Home({ view: view }), "top-left");
  view.ui.add(new Fullscreen({ view: view }), "top-left");
  view.ui.add(new BasemapToggle({ view: view, nextBasemap: "topo-vector" }), "bottom-right");
  view.ui.add(new Expand({ view: view, content: new LayerList({ view: view }), expandTooltip: "Sluoksniai", group: "d" }), "top-right");
  view.ui.add(new Expand({ view: view, content: new Legend({ view: view }), expandTooltip: "Legenda", group: "d" }), "top-right");
  view.ui.add(new Expand({ view: view, content: new Daylight({ view: view }), expandTooltip: "Dienos šviesa", group: "d" }), "top-right");
  view.ui.add(new Expand({ view: view, content: new Weather({ view: view }), expandTooltip: "Orai", group: "d" }), "top-right");

  // ---------------- 5. Paieška 3D ----------------
  const paieska = common.sukurtiPaieska(view, miestai, vietos, apskritys);
  paieska.goToOverride = function (v, params) {
    const g = params.target.target;
    const taskas = g && g.geometry && g.geometry.type === "point";
    const tikslas = taskas ? { target: g.geometry, zoom: 15, tilt: 65 } : { target: g, tilt: 45 };
    return v.goTo(tikslas, { duration: 2500 });
  };
  view.ui.add(paieska, { position: "manual" });
  paieska.container.id = "paieska";
  view.container.appendChild(paieska.container);

  // ---------------- 4. 3D grafika: Gedimino bokštas iš 7 elementų ----------------
  const cx = 25.2350, cy = 54.6845;                 // Vingio parkas
  const mPerLon = 111320 * Math.cos(cy * Math.PI / 180);
  function taskas(dxMetrai, z) {
    return { type: "point", longitude: cx + dxMetrai / mPerLon, latitude: cy, z: z };
  }
  function objektas(primityvas, spalva, plotis, gylis, aukstis) {
    return {
      type: "point-3d",
      symbolLayers: [{
        type: "object",
        resource: { primitive: primityvas },
        width: plotis, depth: gylis, height: aukstis,
        anchor: "bottom",
        material: { color: spalva }
      }]
    };
  }
  const popup = { title: "{pavadinimas}", content: "{aprasymas}" };
  function elementas(geometrija, simbolis, pav, apr) {
    return new Graphic({ geometry: geometrija, symbol: simbolis, attributes: { pavadinimas: pav, aprasymas: apr }, popupTemplate: popup });
  }

  const bokstas = [
    elementas(taskas(0, 0), objektas("cone", [74, 124, 58], 280, 280, 45),
      "Pilies kalnas", "Kūgis (cone) – Gedimino kalno imitacija."),
    elementas(taskas(0, 40), objektas("cylinder", [168, 72, 48], 55, 55, 100),
      "Bokšto korpusas", "Cilindras (cylinder) iš raudonų plytų."),
    elementas(taskas(0, 140), objektas("cylinder", [120, 50, 36], 64, 64, 14),
      "Viršutinis aukštas", "Platesnis cilindras – apžvalgos aikštelė."),
    elementas(taskas(0, 154), objektas("cylinder", [230, 230, 230], 2.5, 2.5, 80),
      "Vėliavos stiebas", "Plonas cilindras – stiebas."),
    elementas(taskas(32, 218), objektas("cube", [253, 185, 19], 60, 1.5, 12),
      "Geltona juosta", "Kubas (cube) – saulė ir gerovė."),
    elementas(taskas(32, 206), objektas("cube", [0, 106, 68], 60, 1.5, 12),
      "Žalia juosta", "Kubas (cube) – gamta ir viltis."),
    elementas(taskas(32, 194), objektas("cube", [193, 39, 45], 60, 1.5, 12),
      "Raudona juosta", "Kubas (cube) – drąsa ir kraujas."),
    elementas(taskas(0, 240), {
      type: "point-3d",
      symbolLayers: [{
        type: "text",
        text: "Gedimino bokštas (modelis)",
        size: 14,
        material: { color: [31, 41, 51] },
        halo: { color: "white", size: 2 }
      }],
      verticalOffset: { screenLength: 50 },
      callout: { type: "line", size: 1.5, color: [31, 41, 51] }
    }, "Užrašas", "Tekstas su nuorodos linija (callout).")
  ];
  grafika.addMany(bokstas);

  // ---------------- 10. Kameros valdymas ----------------
  const greitosVietos = [
    { pav: "Visa Lietuva", camera: { position: { longitude: 23.95, latitude: 52.95, z: 260000 }, heading: 0, tilt: 48 } },
    { pav: "Vilniaus senamiestis", tikslas: [25.2878, 54.6847], zoom: 16, tilt: 70, heading: 330 },
    { pav: "Gedimino bokštas (3D)", camera: { position: { longitude: cx, latitude: cy - 650 / 111320, z: 420 }, heading: 0, tilt: 72 } },
    { pav: "Trakų pilis", tikslas: [24.9336, 54.6525], zoom: 16, tilt: 65, heading: 200 },
    { pav: "Kauno senamiestis", tikslas: [23.8900, 54.8970], zoom: 16, tilt: 65, heading: 60 },
    { pav: "Kuršių nerija", tikslas: [21.03, 55.30], zoom: 12, tilt: 72, heading: 300 }
  ];

  greitosVietos.forEach(function (v) {
    const b = document.createElement("button");
    b.className = "antrinis";
    b.textContent = v.pav;
    b.onclick = function () {
      sustabdytiSkrydi();
      const tikslas = v.camera || { center: v.tikslas, zoom: v.zoom, tilt: v.tilt, heading: v.heading };
      view.goTo(tikslas, { duration: 3000 });
    };
    $("vietos").appendChild(b);
  });
  $("bokstasEiti").onclick = function () { $("vietos").children[2].click(); };

  document.querySelectorAll("[data-kamera]").forEach(function (b) {
    b.onclick = function () {
      sustabdytiSkrydi();
      const c = view.camera;
      const veiksmai = {
        kaire: { heading: c.heading - 30 },
        desine: { heading: c.heading + 30 },
        aukstyn: { tilt: Math.max(0, c.tilt - 15) },
        zemyn: { tilt: Math.min(85, c.tilt + 15) },
        arciau: { zoom: view.zoom + 1 },
        toliau: { zoom: view.zoom - 1 },
        virsus: { tilt: 0, heading: 0 }
      };
      view.goTo(veiksmai[b.dataset.kamera], { duration: 1000 });
    };
  });

  let skrydis = null;
  function sustabdytiSkrydi() {
    if (skrydis) cancelAnimationFrame(skrydis);
    skrydis = null;
    $("skrydis").classList.remove("aktyvus");
  }
  $("skrydis").onclick = function () {
    if (skrydis) return sustabdytiSkrydi();
    $("skrydis").classList.add("aktyvus");
    const zingsnis = function () {
      view.goTo({ heading: view.camera.heading + 0.25 }, { animate: false });
      skrydis = requestAnimationFrame(zingsnis);
    };
    zingsnis();
  };
  view.on("drag", sustabdytiSkrydi);

  reactiveUtils.watch(() => view.camera, function (c) {
    if (!c) return;
    const p = c.position;
    $("kameraInfo").textContent =
      "Padėtis: " + p.latitude.toFixed(4) + "° š. pl., " + p.longitude.toFixed(4) + "° r. ilg.\n" +
      "Aukštis: " + Math.round(p.z).toLocaleString("lt-LT") + " m · Kryptis: " + Math.round(c.heading) +
      "° · Pokrypis: " + Math.round(c.tilt) + "°";
  }, { initial: true });

  // ---------------- 9. Realistinė aplinka ----------------
  // Vietinį Lietuvos laiką (Europe/Vilnius) paverčiame UTC Date objektu.
  function vilniausData(dataStr, minutes) {
    const [y, m, d] = dataStr.split("-").map(Number);
    const spejimas = Date.UTC(y, m - 1, d, Math.floor(minutes / 60), minutes % 60);
    const dalys = new Intl.DateTimeFormat("en-US", {
      timeZone: "Europe/Vilnius", hourCycle: "h23",
      year: "numeric", month: "numeric", day: "numeric", hour: "numeric", minute: "numeric"
    }).formatToParts(new Date(spejimas)).reduce(function (o, p) { o[p.type] = +p.value; return o; }, {});
    const vietinis = Date.UTC(dalys.year, dalys.month - 1, dalys.day, dalys.hour, dalys.minute);
    const poslinkisVal = (vietinis - spejimas) / 3600000;
    return { date: new Date(spejimas - poslinkisVal * 3600000), utcPoslinkis: poslinkisVal };
  }

  function taikytiApsvietima() {
    const min = +$("laikas").value;
    $("laikasTxt").textContent = String(Math.floor(min / 60)).padStart(2, "0") + ":" + String(min % 60).padStart(2, "0");
    const env = view.environment;
    if ($("apsvietimas").value === "sun") {
      const vd = vilniausData($("data").value || "2026-06-21", min);
      env.lighting = {
        type: "sun",
        date: vd.date,
        displayUTCOffset: vd.utcPoslinkis,
        directShadowsEnabled: $("seseliai").checked
      };
    } else {
      env.lighting = { type: "virtual", directShadowsEnabled: $("seseliai").checked };
    }
  }

  function taikytiOrus() {
    const tipas = $("orai").value;
    const parametrai = {
      sunny: { type: "sunny", cloudCover: 0.2 },
      cloudy: { type: "cloudy", cloudCover: 0.8 },
      rainy: { type: "rainy", cloudCover: 0.8, precipitation: 0.6 },
      snowy: { type: "snowy", cloudCover: 0.8, precipitation: 0.5, snowCover: "enabled" },
      foggy: { type: "foggy", fogStrength: 0.6 }
    };
    view.environment.weather = parametrai[tipas];
  }

  ["laikas", "data", "apsvietimas", "seseliai"].forEach(function (id) { $(id).addEventListener("input", taikytiApsvietima); });
  $("orai").onchange = taikytiOrus;
  $("zvaigzdes").onchange = function () { view.environment.starsEnabled = this.checked; };
  $("atmosfera").onchange = function () { view.environment.atmosphereEnabled = this.checked; };
  document.querySelectorAll("[data-laikas]").forEach(function (b) {
    b.onclick = function () {
      $("laikas").value = b.dataset.laikas;
      taikytiApsvietima();
    };
  });

  view.when(function () {
    taikytiApsvietima();
    taikytiOrus();
  });

  window.gisView = view;
  window.gisPaieska = paieska;
});

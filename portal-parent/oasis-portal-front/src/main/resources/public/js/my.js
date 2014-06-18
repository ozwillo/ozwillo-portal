$("a.nav-link").hover(
  function() {
      var image_purple = $(this).find("img.purple");
      image_purple.fadeIn(250);
  },
  function() {
      var image_purple = $(this).find("img.purple");
      image_purple.fadeOut(250);
  }
);
// ============================================================================
// TICKET-ADV100 — Dark/Light Theme Toggle
// Persists theme in localStorage and restores it on page load.
// ============================================================================

(function () {

  const storedTheme = localStorage.getItem("reconx-theme") || "light";
  document.documentElement.setAttribute("data-theme", storedTheme);

  document.addEventListener("DOMContentLoaded", () => {

    const themeButton = document.getElementById("theme-toggle");

    if (!themeButton) {
      return;
    }

    themeButton.setAttribute(
        "aria-pressed",
        storedTheme === "dark"
    );

    themeButton.addEventListener("click", () => {

      const currentTheme =
          document.documentElement.getAttribute("data-theme");

      const nextTheme =
          currentTheme === "light"
              ? "dark"
              : "light";

      document.documentElement.setAttribute(
          "data-theme",
          nextTheme
      );

      localStorage.setItem(
          "reconx-theme",
          nextTheme
      );

      themeButton.setAttribute(
          "aria-pressed",
          nextTheme === "dark"
      );

    });

  });

})();
(() => {
  const button = document.querySelector('.menu-button');
  const navigation = document.querySelector('#navigation');
  const wide = matchMedia('(min-width: 980px)');
  function setNavigation() {
    if (!button || !navigation) return;
    button.hidden = wide.matches;
    navigation.hidden = !wide.matches;
    button.setAttribute('aria-expanded', String(wide.matches));
  }
  setNavigation();
  wide.addEventListener('change', setNavigation);
  button?.addEventListener('click', () => {
    navigation.hidden = !navigation.hidden;
    button.setAttribute('aria-expanded', String(!navigation.hidden));
  });
  document.addEventListener('keydown', event => {
    if (event.key === 'Escape' && !wide.matches && navigation && !navigation.hidden) {
      navigation.hidden = true;
      button.setAttribute('aria-expanded', 'false');
      button.focus();
    }
  });
  document.querySelectorAll('form[method="post"]').forEach(form => {
    form.addEventListener('submit', () => {
      const submit = form.querySelector('button[type="submit"]');
      if (submit) submit.disabled = true;
      const status = form.querySelector('.loading');
      if (status) status.hidden = false;
      form.setAttribute('aria-busy', 'true');
    });
  });
  addEventListener('pageshow', () => {
    document.querySelectorAll('form[aria-busy]').forEach(form => {
      form.removeAttribute('aria-busy');
      form.querySelectorAll('button[type="submit"]').forEach(button => button.disabled = false);
      form.querySelectorAll('.loading').forEach(status => status.hidden = true);
    });
  });
  document.querySelector('[role="alert"]')?.focus();
})();

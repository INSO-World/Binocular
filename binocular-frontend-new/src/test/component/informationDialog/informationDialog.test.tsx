import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import InformationDialog from '../../../components/informationDialog/informationDialog';

describe('InformationDialog', () => {
  it('C13.1 renders a <dialog> element', () => {
    render(<InformationDialog />);
    expect(document.querySelector('dialog')).not.toBeNull();
  });

  it('C13.2 dialog has id="informationDialog"', () => {
    render(<InformationDialog />);
    expect(document.getElementById('informationDialog')).not.toBeNull();
  });

  it('C13.3 has an element with id informationDialogHeadline', () => {
    render(<InformationDialog />);
    expect(document.getElementById('informationDialogHeadline')).not.toBeNull();
  });

  it('C13.4 has an element with id informationDialogText', () => {
    render(<InformationDialog />);
    expect(document.getElementById('informationDialogText')).not.toBeNull();
  });

  it('C13.5 renders a Close button', () => {
    render(<InformationDialog />);
    expect(screen.getAllByRole('button', { name: /close/i, hidden: true }).length).toBeGreaterThan(0);
  });
});

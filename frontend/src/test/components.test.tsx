import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import OtpInput from '../components/OtpInput';
import ProgressStepper from '../components/ProgressStepper';

describe('OtpInput', () => {
    it('renders 6 input fields', () => {
        render(
            <OtpInput
                length={6}
                value=""
                onChange={() => { }}
            />
        );

        const inputs = screen.getAllByRole('textbox');
        expect(inputs).toHaveLength(6);
    });

    it('updates value on input', () => {
        let currentValue = '';
        const handleChange = (value: string) => {
            currentValue = value;
        };

        const { rerender } = render(
            <OtpInput
                length={6}
                value={currentValue}
                onChange={handleChange}
            />
        );

        const inputs = screen.getAllByRole('textbox');
        fireEvent.change(inputs[0], { target: { value: '1' } });

        expect(currentValue).toBe('1');

        rerender(
            <OtpInput
                length={6}
                value={currentValue}
                onChange={handleChange}
            />
        );

        expect(inputs[0]).toHaveValue('1');
    });

    it('handles paste event', () => {
        let currentValue = '';
        const handleChange = (value: string) => {
            currentValue = value;
        };

        render(
            <OtpInput
                length={6}
                value={currentValue}
                onChange={handleChange}
            />
        );

        const inputs = screen.getAllByRole('textbox');

        // Simulate paste
        fireEvent.paste(inputs[0], {
            clipboardData: {
                getData: () => '123456',
            },
        });

        expect(currentValue).toBe('123456');
    });

    it('filters non-numeric input', () => {
        let currentValue = '';
        const handleChange = (value: string) => {
            currentValue = value;
        };

        render(
            <OtpInput
                length={6}
                value={currentValue}
                onChange={handleChange}
            />
        );

        const inputs = screen.getAllByRole('textbox');
        fireEvent.change(inputs[0], { target: { value: 'abc' } });

        // Should filter out non-numeric characters
        expect(currentValue).toBe('');
    });

    it('is disabled when prop is set', () => {
        render(
            <OtpInput
                length={6}
                value=""
                onChange={() => { }}
                disabled
            />
        );

        const inputs = screen.getAllByRole('textbox');
        inputs.forEach(input => {
            expect(input).toBeDisabled();
        });
    });
});

describe('ProgressStepper', () => {
    const steps = ['Step 1', 'Step 2', 'Step 3', 'Step 4'];

    it('renders all steps', () => {
        render(<ProgressStepper currentStep={1} steps={steps} />);

        const stepCircles = document.querySelectorAll('.step-circle');
        expect(stepCircles).toHaveLength(4);
    });

    it('marks current step as active', () => {
        render(<ProgressStepper currentStep={2} steps={steps} />);

        const stepElements = document.querySelectorAll('.step');
        expect(stepElements[1]).toHaveClass('active');
    });

    it('marks previous steps as completed', () => {
        render(<ProgressStepper currentStep={3} steps={steps} />);

        const stepElements = document.querySelectorAll('.step');
        expect(stepElements[0]).toHaveClass('completed');
        expect(stepElements[1]).toHaveClass('completed');
        expect(stepElements[2]).toHaveClass('active');
        expect(stepElements[3]).not.toHaveClass('completed');
    });

    it('shows checkmark for completed steps', () => {
        render(<ProgressStepper currentStep={3} steps={steps} />);

        const stepCircles = document.querySelectorAll('.step-circle');
        expect(stepCircles[0]).toHaveTextContent('✓');
        expect(stepCircles[1]).toHaveTextContent('✓');
        expect(stepCircles[2]).toHaveTextContent('3');
    });
});
